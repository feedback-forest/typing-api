package dasi.typing.handler;

import dasi.typing.api.service.oauth.CustomOidcUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final int TTL = 3;
  private final String REDIRECT_URL = "/oauth/login";
  private final String REDIS_KEY_PREFIX = "auth:temp-token:";

  private final RedisTemplate<String, String> redisTemplate;
  private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    CustomOidcUser customUser = (CustomOidcUser) authentication.getPrincipal();

    String tempToken = UUID.randomUUID().toString();
    String targetUrl = getTargetUrl(tempToken);

    String kakaoId = customUser.getInfo().getSub();
    String redisKey = REDIS_KEY_PREFIX + tempToken;
    redisTemplate.opsForValue().set(
        redisKey,
        kakaoId,
        TTL,
        TimeUnit.MINUTES
    );

    redirectStrategy.sendRedirect(request, response, targetUrl);
  }

  private String getTargetUrl(String tempToken) {
    return UriComponentsBuilder
        .fromUriString(REDIRECT_URL)
        .queryParam("success", tempToken)
        .build().toUriString();
  }
}

