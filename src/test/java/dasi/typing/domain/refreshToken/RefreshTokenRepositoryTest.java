package dasi.typing.domain.refreshToken;

import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RefreshTokenRepositoryTest {

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Test
  @DisplayName("kakaoId로 Redis에 RefreshToken을 저장한 후, 해당 키로 정상 조회되어야 한다.")
  void redisTokenSaveTest() {
    // given
    String kakaoId = "1234567890";
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId);
    String token = jwtToken.getRefreshToken();

    // when
    RefreshToken savedRefreshToken = refreshTokenRepository.save(RefreshToken.builder()
        .kakaoId(kakaoId)
        .token(token).build());

    RefreshToken refreshToken = refreshTokenRepository.findByKakaoId(kakaoId).orElseThrow(
        () -> new CustomException(Code.EXPIRED_REFRESH_TOKEN)
    );

    // then
    assertThat(savedRefreshToken.getToken()).isEqualTo(refreshToken.getToken());
  }
}