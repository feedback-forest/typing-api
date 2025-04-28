package dasi.typing.api.service.member;

import static dasi.typing.domain.consent.ConsentType.PRIVACY_POLICY;
import static dasi.typing.domain.consent.ConsentType.TERMS_OF_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.refreshToken.RefreshToken;
import dasi.typing.domain.refreshToken.RefreshTokenRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtTokenProvider;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @AfterEach
  void tearDown() {
    memberRepository.deleteAllInBatch();
    refreshTokenRepository.deleteAll();
  }

  @Test
  @DisplayName("동시에 같은 닉네임으로 요청하면 1명만 성공한다")
  void nicknameConcurrency() throws Exception {
    // given
    int threadCount = 100;
    String nickname = "닉네임";
    String tempToken = UUID.randomUUID().toString();
    String kakaoId = "0000000000";

    saveKakaoIdInRedis(tempToken, kakaoId);

    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failedCount = new AtomicInteger(0);

    MemberCreateServiceRequest request = MemberCreateServiceRequest.builder()
        .nickname(nickname)
        .agreements(List.of(TERMS_OF_SERVICE, PRIVACY_POLICY)).build();

    // when
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          memberService.signUp(tempToken, request);
          successCount.incrementAndGet();
        } catch (DataIntegrityViolationException e) {
          failedCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
    executorService.shutdown();

    // then
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failedCount.get()).isEqualTo(threadCount - 1);
  }

  @Test
  @DisplayName("존재하지 않는 RefreshToken으로 재발급 요청 시 EXPIRED_REFRESH_TOKEN 예외가 발생한다")
  void expiredRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";

    // when & then
    assertThatThrownBy(() -> memberService.reissue(kakaoId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(Code.EXPIRED_REFRESH_TOKEN.getMessage());
  }

  @Test
  @DisplayName("RefreshToken이 유효하지 않으면 INVALID_REFRESH_TOKEN 예외가 발생한다")
  void refreshTokenInvalidTest() {
    // given
    String kakaoId = "kakaoId123";
    RefreshToken refreshToken = RefreshToken.builder()
        .kakaoId(kakaoId)
        .token("INVALID_REFRESH_TOKEN").build();
    refreshTokenRepository.save(refreshToken);

    // when & then
    assertThatThrownBy(() -> memberService.reissue(kakaoId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(Code.INVALID_REFRESH_TOKEN.getMessage());
  }

  @Test
  @DisplayName("AccessToken이 만료되고, RefreshToken이 유효한하다면 토큰을 재발급한다.")
  void accessTokenReissueByRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    String refreshToken = jwtTokenProvider.generateToken(kakaoId).getRefreshToken();

    // when
    String accessToken = memberService.reissue(kakaoId);
    RefreshToken newRefreshToken = refreshTokenRepository.findByKakaoId(kakaoId).orElseThrow(
        () -> new CustomException(Code.INVALID_REFRESH_TOKEN)
    );

    // then
    assertThat(accessToken).isNotNull();
    assertThat(newRefreshToken.getToken()).isNotEqualTo(refreshToken);
  }


  private void saveKakaoIdInRedis(String tempToken, String kakaoId) {
    redisTemplate.opsForValue().set(
        tempToken,
        kakaoId,
        10,
        TimeUnit.SECONDS
    );
  }
}