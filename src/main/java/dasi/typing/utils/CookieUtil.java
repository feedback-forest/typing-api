package dasi.typing.utils;

import static dasi.typing.utils.ConstantUtil.ACCESS_TOKEN_COOKIE;
import static dasi.typing.utils.ConstantUtil.REFRESH_TOKEN_COOKIE;
import static dasi.typing.utils.ConstantUtil.TOKEN_EXPIRE_TIME;
import static dasi.typing.utils.ConstantUtil.TOKEN_REFRESH_TIME;

import dasi.typing.jwt.JwtToken;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  @Value("${cookie.secure:true}")
  private boolean secureCookie;

  private static boolean secure = true;

  @PostConstruct
  public void init() {
    secure = secureCookie;
  }

  public static void addTokenCookies(HttpServletResponse response, JwtToken jwtToken) {
    ResponseCookie accessCookie = createTokenCookie(ACCESS_TOKEN_COOKIE, jwtToken.accessToken(), TOKEN_EXPIRE_TIME / 1000);
    ResponseCookie refreshCookie = createTokenCookie(REFRESH_TOKEN_COOKIE, jwtToken.refreshToken(), TOKEN_REFRESH_TIME / 1000);

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  public static void clearTokenCookies(HttpServletResponse response) {
    ResponseCookie accessCookie = createTokenCookie(ACCESS_TOKEN_COOKIE, "", 0);
    ResponseCookie refreshCookie = createTokenCookie(REFRESH_TOKEN_COOKIE, "", 0);

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  public static String resolveTokenFromCookie(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return null;
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookieName.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  private static ResponseCookie createTokenCookie(String name, String value, long maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Lax")
        .path("/")
        .maxAge(maxAge)
        .build();
  }
}
