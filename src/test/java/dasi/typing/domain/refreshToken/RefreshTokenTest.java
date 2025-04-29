package dasi.typing.domain.refreshToken;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RefreshTokenTest {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Test
  @DisplayName("RefreshToken TTL 시간이 만료되면 조회 시 예외가 발생해야 한다.")
  void redisRefreshTokenTTLExpirationTest() throws Exception {
    // given
    String kakaoId = "1234567890";
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId);
    String refreshToken = jwtToken.getRefreshToken();

    // when
    redisTemplate.opsForValue().set(kakaoId, refreshToken, Duration.ofSeconds(1));
    Thread.sleep(2000);

    // then
    assertThatThrownBy(() -> {
      String token = redisTemplate.opsForValue().get(kakaoId);
      if (token == null) {
        throw new CustomException(Code.EXPIRED_REFRESH_TOKEN);
      }
    }).isInstanceOf(CustomException.class)
        .hasMessageContaining(Code.EXPIRED_REFRESH_TOKEN.getMessage());
  }

}