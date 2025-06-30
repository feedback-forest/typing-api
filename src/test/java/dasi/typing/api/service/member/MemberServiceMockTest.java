package dasi.typing.api.service.member;

import static dasi.typing.exception.Code.INVALID_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dasi.typing.domain.refreshToken.RefreshToken;
import dasi.typing.domain.refreshToken.RefreshTokenRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class MemberServiceMockTest {

  @MockitoSpyBean
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private MemberService memberService;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Test
  @DisplayName("유효한 Refresh 토큰을 검증하면 true를 반환한다")
  void validateRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, new Date());
    RefreshToken refreshToken = new RefreshToken(kakaoId, jwtToken.refreshToken());
    refreshTokenRepository.save(refreshToken);

    // when
    when(jwtTokenProvider.validateRefreshToken(refreshToken.getToken()))
        .thenReturn(true);

    // then
    assertThatCode(() -> memberService.reissue(kakaoId))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("유효하지 않은 Refresh 토큰을 검증하면 INVALID_REFRESH_TOKEN 예외를 발생시킨다")
  void validateInvalidRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    String invalidToken = "invalid_refresh_token";
    RefreshToken refreshToken = new RefreshToken(kakaoId, invalidToken);
    refreshTokenRepository.save(refreshToken);

    // when
    doThrow(new CustomException(INVALID_REFRESH_TOKEN))
        .when(jwtTokenProvider).validateRefreshToken(refreshToken.getToken());

    Code expectedErrorCode = assertThrows(
        CustomException.class, () -> memberService.reissue(kakaoId))
        .getErrorCode();

    // then
    assertThat(expectedErrorCode).isEqualTo(INVALID_REFRESH_TOKEN);
  }
}