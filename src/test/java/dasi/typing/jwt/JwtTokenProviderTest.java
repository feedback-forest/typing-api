package dasi.typing.jwt;

import static dasi.typing.exception.Code.EMPTY_JWT_TOKEN;
import static dasi.typing.exception.Code.EXPIRED_ACCESS_TOKEN;
import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.UNSUPPORTED_JWT_TOKEN;
import static dasi.typing.utils.ConstantUtil.TOKEN_EXPIRE_TIME;
import static dasi.typing.utils.ConstantUtil.TOKEN_REFRESH_TIME;
import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dasi.typing.exception.CustomException;
import dasi.typing.jwt.response.ClaimsResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtTokenProviderTest {

  @Autowired
  JwtTokenProvider jwtTokenProvider;

  @Test
  @DisplayName("JWT 토큰을 통해서 Claim 정보를 추출할 수 있다.")
  void extractClaimsFromTokenTest() {
    // given
    String kakaoId = "1234567890";

    // when
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, new Date());
    Claims claims = jwtTokenProvider.getClaimsResponse(jwtToken.accessToken()).claims();

    // then
    assertThat(claims).extracting("issuer", "subject")
        .containsExactly("typing", "KAKAO_ID");
  }

  @Test
  @DisplayName("만료 시간이 지난 JWT 토큰을 통해서 Claim 정보를 추출하면 ClaimsResponse가 만료된 토큰임을 알려준다.")
  void extractClaimsFromExpiredTokenTest() {
    // given
    String kakaoId = "1234567890";
    Date now = new Date();
    JwtToken jwtToken = jwtTokenProvider
        .generateToken(kakaoId, new Date(now.getTime() - TOKEN_EXPIRE_TIME * 2));

    // when
    String expiredToken = jwtToken.accessToken();
    ClaimsResponse response = jwtTokenProvider.getClaimsResponse(expiredToken);

    // then
    assertThat(response)
        .extracting("claims", "expired")
        .containsExactly(jwtTokenProvider.getClaimsResponse(expiredToken).claims(), true);
  }

  @Test
  @DisplayName("JWT 토큰을 통해서 kakaoId 정보를 추출할 수 있다.")
  void extractKakaoIdFromToken() {
    // given
    String kakaoId = "1234567890";

    // when
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, new Date());
    String extractedKakaoId = jwtTokenProvider.getKakaoId(jwtToken.accessToken());

    // then
    assertThat(extractedKakaoId).isEqualTo(kakaoId);
  }

  @Test
  @DisplayName("유효한 ACCESS JWT 토큰은 검증 결과로 true를 반환한다.")
  void isValidTokenTest() {
    // given
    String kakaoId = "1234567890";

    // when
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, new Date());
    boolean isValid = jwtTokenProvider.validateAccessToken(jwtToken.accessToken());

    // then
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("토큰 자체가 조작되어 변형된 경우 INVALID_ACCESS_TOKEN 예외가 발생한다.")
  void isValidTokenWithTamperedTokenTest() {
    // given: 암호화 키가 조작된 경우
    String invalidToken = Jwts.builder()
        .setSubject("KAKAO_ID")
        .signWith(hmacShaKeyFor("wrong-signing-key-wrong-signing-key".getBytes()), HS256)
        .compact();

    // when & then
    assertThrows(CustomException.class, () -> jwtTokenProvider.validateAccessToken(invalidToken));
  }

  @Test
  @DisplayName("만료된 ACCESS JWT 토큰은 검증 결과로 EXPIRED_REFRESH_TOKEN 예외를 발생시킨다.")
  void isValidExpiredTokenTest() {
    // given
    String kakaoId = "1234567890";
    Date now = new Date();
    JwtToken jwtToken = jwtTokenProvider
        .generateToken(kakaoId, new Date(now.getTime() - TOKEN_EXPIRE_TIME * 2));

    // when
    String expiredToken = jwtToken.accessToken();
    CustomException exception = assertThrows(CustomException.class,
        () -> jwtTokenProvider.validateAccessToken(expiredToken)
    );

    // then
    assertThat(exception.getErrorCode()).isEqualTo(EXPIRED_ACCESS_TOKEN);
  }

  @Test
  @DisplayName("지원하지 않는 형식의 ACCESS JWT 토큰은 검증 결과로 UNSUPPORTED_JWT_TOKEN 예외를 발생시킨다.")
  void isValidUnsupportedTokenTest() {
    // given: alg = none 이거나 비정상 형식의 지원하지 않는 JWT 토큰
    String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jwtTokenProvider.validateAccessToken(unsupportedToken)
    );

    // then
    assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_JWT_TOKEN);
  }

  @Test
  @DisplayName("빈 ACCESS JWT 토큰은 검증 결과로 EMPTY_JWT_TOKEN 예외를 발생시킨다.")
  void isValidEmptyTokenTest() {
    // given
    String emptyToken = "";

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jwtTokenProvider.validateAccessToken(emptyToken)
    );

    // then
    assertThat(exception.getErrorCode()).isEqualTo(EMPTY_JWT_TOKEN);
  }

  @Test
  @DisplayName("유효한 REFRESH JWT 토큰은 검증 결과로 true를 반환한다.")
  void isValidRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, new Date());

    // when
    String refreshToken = jwtToken.refreshToken();
    boolean isValid = jwtTokenProvider.validateRefreshToken(refreshToken);

    // then
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("토큰 자체가 조작되어 변형된 경우 INVALID_REFRESH_TOKEN 예외가 발생한다.")
  void isValidRefreshTokenWithTamperedTokenTest() {
    // given: 암호화 키가 조작된 경우
    String invalidToken = Jwts.builder()
        .setSubject("KAKAO_ID")
        .signWith(hmacShaKeyFor("wrong-signing-key-wrong-signing-key".getBytes()), HS256)
        .compact();

    // when & then
    assertThrows(CustomException.class, () -> jwtTokenProvider.validateRefreshToken(invalidToken));
  }

  @Test
  @DisplayName("만료된 REFRESH JWT 토큰은 검증 결과로 EXPIRED_REFRESH_TOKEN 예외를 발생시킨다.")
  void isValidExpiredRefreshTokenTest() {
    // given
    String kakaoId = "1234567890";
    Date now = new Date();
    JwtToken jwtToken = jwtTokenProvider
        .generateToken(kakaoId, new Date(now.getTime() - TOKEN_REFRESH_TIME * 2));

    // when
    String expiredToken = jwtToken.refreshToken();
    CustomException exception = assertThrows(CustomException.class,
        () -> jwtTokenProvider.validateRefreshToken(expiredToken)
    );

    // then
    assertThat(exception.getErrorCode()).isEqualTo(EXPIRED_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("지원하지 않는 형식의 REFRESH JWT 토큰은 검증 결과로 UNSUPPORTED_JWT_TOKEN 예외를 발생시킨다.")
  void isValidUnsupportedRefreshTokenTest() {
    // given: alg = none 이거나 비정상 형식의 지원하지 않는 JWT 토큰
    String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jwtTokenProvider.validateRefreshToken(unsupportedToken)
    );

    // then
    assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_JWT_TOKEN);
  }

  @Test
  @DisplayName("빈 REFRESH JWT 토큰은 검증 결과로 EMPTY_JWT_TOKEN 예외를 발생시킨다.")
  void isValidEmptyRefreshTokenTest() {
    // given
    String emptyToken = "";

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jwtTokenProvider.validateRefreshToken(emptyToken)
    );

    // then
    assertThat(exception.getErrorCode()).isEqualTo(EMPTY_JWT_TOKEN);
  }
}