package dasi.typing.utils;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ConstantUtil {

  // Header Prefix
  public static final String TOKEN_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  // Redis Key Prefix
  public static final String REDIS_KEY_PREFIX = "auth:temp-token:";

  // Temporary Token TTL
  public static final int TEMP_TOKEN_TTL = 3;

  // Login Redirect URL
  public static final String LOGIN_REDIRECT_URL = "/typing/login/callback";

  // Consent
  public static final int REQUIRED_CONSENT_COUNT = 3;

  // Reissue URI
  public static final String REISSUE_URI = "/api/v1/members/reissue";

  // User Info URL
  public static final String USER_INFO_URL = "https://kapi.kakao.com/v1/oidc/userinfo";

  // JWT Token Expiration and Refresh Times
  public static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 3;
  public static final long TOKEN_REFRESH_TIME = 1000 * 60 * 60 * 24 * 7;

}