package dasi.typing.api.service.member;

import static dasi.typing.domain.consent.ConsentType.AGE_LIMIT_POLICY;
import static dasi.typing.domain.consent.ConsentType.PRIVACY_POLICY;
import static dasi.typing.domain.consent.ConsentType.TERMS_OF_SERVICE;
import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INSUFFICIENT_CONSENT_EXCEPTION;
import static dasi.typing.exception.Code.INVALID_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INVALID_TEMP_TOKEN;
import static dasi.typing.exception.Code.KAKAO_ACCOUNT_NOT_REGISTERED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.api.service.member.request.MemberNicknameServiceRequest;
import dasi.typing.api.service.member.validator.NicknameValidator;
import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentRepository;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.memberConsent.MemberConsentRepository;
import dasi.typing.domain.refreshToken.RefreshToken;
import dasi.typing.domain.refreshToken.RefreshTokenRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MemberServiceTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private MemberConsentRepository memberConsentRepository;

  @Autowired
  private ConsentRepository consentRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  NicknameValidator nicknameValidator;

  @BeforeEach
  void setUp() {
    consentSetup();
  }

  @AfterEach
  void tearDown() {
    memberConsentRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    consentRepository.deleteAllInBatch();
    refreshTokenRepository.deleteAll();
  }

  @Test
  @DisplayName("유효하지 임시 토큰을 사용하면 INVALID_TEMP_TOKEN 예외가 발생한다.")
  void signInWithInvalidTempTokenTest() {
    // given
    String invalidTempToken = "invalid_token";
    Code expectedCode = INVALID_TEMP_TOKEN;

    // when
    Code result = assertThrows(CustomException.class, () -> {
      memberService.signIn(invalidTempToken);
    }).getErrorCode();

    // then
    assertThat(result)
        .extracting("code", "message")
        .containsExactly(expectedCode.getCode(), expectedCode.getMessage());
  }

  @Test
  @DisplayName("회원가입을 하지 않은 유저는 로그인 시 KAKAO_ACCOUNT_NOT_REGISTERED 예외가 발생한다.")
  void signInWithoutSignUpTest() {
    // given
    String tempToken = UUID.randomUUID().toString();
    saveKakaoIdInRedis(tempToken, "1234567890");
    Code expectedCode = KAKAO_ACCOUNT_NOT_REGISTERED;

    // when
    Code result = assertThrows(CustomException.class, () -> {
      memberService.signIn(tempToken);
    })
        .getErrorCode();

    // then
    assertThat(result)
        .extracting("code", "message")
        .containsExactly(expectedCode.getCode(), expectedCode.getMessage());
  }

  @Test
  @DisplayName("로그인 시 kakaoID를 가진 회원이 존재하면 AccessToken을 반환한다.")
  void signInWithRegisteredMemberTest() {
    // given
    String tempToken = UUID.randomUUID().toString();
    String kakaoId = "1234567890";
    memberRepository.save(new Member(kakaoId, "testNickname"));

    // when
    saveKakaoIdInRedis(tempToken, kakaoId);
    String accessToken = memberService.signIn(tempToken);

    // then
    assertThat(accessToken).isNotNull();
  }

  @Test
  @DisplayName("회원가입 시 동의한 약관의 개수가 충분하지 않으면 INSUFFICIENT_CONSENT_EXCEPTION 예외가 발생한다.")
  void insufficientConsentExceptionTest() {
    // given
    String tempToken = UUID.randomUUID().toString();
    String kakaoId = "1234567890";
    saveKakaoIdInRedis(tempToken, kakaoId);

    MemberCreateServiceRequest request = MemberCreateServiceRequest.builder()
        .nickname("testNickname")
        .agreements(List.of(TERMS_OF_SERVICE, PRIVACY_POLICY))
        .build();

    // when & then
    assertThatThrownBy(() -> memberService.signUp(tempToken, request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(INSUFFICIENT_CONSENT_EXCEPTION.getMessage());
  }

  @Test
  @DisplayName("회원 가입 시 모든 약관에 동의하면 회원이 생성된다.")
  void signUpWithAllAgreementsCreatesMemberTest() {
    // given
    String tempToken = UUID.randomUUID().toString();
    String kakaoId = "1234567890";
    saveKakaoIdInRedis(tempToken, kakaoId);

    MemberCreateServiceRequest request = MemberCreateServiceRequest.builder()
        .nickname("testNickname")
        .agreements(List.of(TERMS_OF_SERVICE, PRIVACY_POLICY, AGE_LIMIT_POLICY))
        .build();

    // when
    memberService.signUp(tempToken, request);
    Member member = memberRepository.findByKakaoId(kakaoId).orElseThrow(
        () -> new CustomException(KAKAO_ACCOUNT_NOT_REGISTERED)
    );

    // then
    assertThat(member).extracting("kakaoId", "nickname")
        .containsExactly(kakaoId, "testNickname");
  }

  @Test
  @DisplayName("회원 가입 시 모든 약관에 동의하면 회원이 생성되고, AccessToken을 반환한다.")
  void signUpWithAllAgreementsTest() {
    // given
    String tempToken = UUID.randomUUID().toString();
    String kakaoId = "1234567890";
    saveKakaoIdInRedis(tempToken, kakaoId);

    MemberCreateServiceRequest request = MemberCreateServiceRequest.builder()
        .nickname("testNickname")
        .agreements(List.of(TERMS_OF_SERVICE, PRIVACY_POLICY, AGE_LIMIT_POLICY))
        .build();

    // when
    String accessToken = memberService.signUp(tempToken, request);

    // then
    assertThat(accessToken).isNotNull();
  }

  @Test
  @DisplayName("유효한 닉네임에 대해서 닉네임 관련 예외가 발생하지 않는다")
  void validateValidNicknameTest() {
    // given
    String validNickname = "validNickname";
    MemberNicknameServiceRequest request = new MemberNicknameServiceRequest(validNickname);

    // when & then
    assertThatCode(() -> memberService.validateNickname(request))
        .doesNotThrowAnyException();

    verify(nicknameValidator).validateLength(validNickname);
    verify(nicknameValidator).validateNoConsonantVowelOnly(validNickname);
    verify(nicknameValidator).validateAllowedCharacters(validNickname);
    verify(nicknameValidator).validateNotDuplicated(validNickname);
  }

  @Test
  @DisplayName("동시에 같은 닉네임으로 요청하면 1명만 성공한다")
  void nicknameConcurrencyTest() throws Exception {
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
        .agreements(List.of(TERMS_OF_SERVICE, PRIVACY_POLICY, AGE_LIMIT_POLICY)).build();

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
  @DisplayName("존재하지 않는 RefreshToken 활용한 재발급 요청 시 EXPIRED_REFRESH_TOKEN 예외가 발생한다")
  void expiredRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";

    // when
    Code expectedErrorCode = assertThrows(CustomException.class, () -> {
      memberService.reissue(kakaoId);
    }).getErrorCode();

    // then
    assertThat(expectedErrorCode).isEqualTo(EXPIRED_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("RefreshToken이 유효하지 않으면 INVALID_REFRESH_TOKEN 예외가 발생한다")
  void refreshTokenInvalidTest() {
    // given
    String kakaoId = "kakaoId123";
    RefreshToken refreshToken = new RefreshToken(kakaoId, "INVALID_REFRESH_TOKEN");
    refreshTokenRepository.save(refreshToken);

    // when
    Code expectedErrorCode = assertThrows(CustomException.class, () -> {
      memberService.reissue(kakaoId);
    }).getErrorCode();

    // then
    assertThat(expectedErrorCode).isEqualTo(INVALID_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("유효한 Refresh 토큰을 검증하면 아무런 예외가 발생하지 않는다.")
  void validRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, new Date());
    RefreshToken refreshToken = new RefreshToken(kakaoId, jwtToken.refreshToken());
    refreshTokenRepository.save(refreshToken);

    // when & then
    assertThatCode(() -> memberService.reissue(kakaoId))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("AccessToken이 만료되고, RefreshToken이 유효한하다면 토큰을 재발급한다.")
  void accessTokenReissueByRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    String refreshToken = jwtTokenProvider.generateToken(kakaoId, new Date()).refreshToken();

    // when
    String accessToken = memberService.reissue(kakaoId);
    RefreshToken newRefreshToken = refreshTokenRepository.findByKakaoId(kakaoId).orElseThrow(
        () -> new CustomException(INVALID_REFRESH_TOKEN)
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

  private void consentSetup() {
    consentRepository.saveAll(List.of(
        new Consent(TERMS_OF_SERVICE),
        new Consent(PRIVACY_POLICY),
        new Consent(AGE_LIMIT_POLICY)
    ));
  }
}