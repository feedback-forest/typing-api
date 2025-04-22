package dasi.typing.api.service.member;

import static dasi.typing.domain.consent.ConsentType.PRIVACY_POLICY;
import static dasi.typing.domain.consent.ConsentType.TERMS_OF_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.domain.member.MemberRepository;
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
  private RedisTemplate<String, String> redisTemplate;

  @AfterEach
  void tearDown() {
    memberRepository.deleteAllInBatch();
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

  private void saveKakaoIdInRedis(String tempToken, String kakaoId) {
    redisTemplate.opsForValue().set(
        tempToken,
        kakaoId,
        10,
        TimeUnit.SECONDS
    );
  }
}