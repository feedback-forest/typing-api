package dasi.typing.handler;

import static dasi.typing.utils.CommonConstant.LOGIN_REDIRECT_URL;
import static dasi.typing.utils.CommonConstant.REDIS_KEY_PREFIX;
import static dasi.typing.utils.CommonConstant.TEMP_TOKEN_TTL;

import dasi.typing.api.service.oauth.CustomOidcUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Value("${front.server}")
  private String FRONT_SERVER;

  private final RedirectStrategy redirectStrategy;
  private final RedisTemplate<String, String> redisTemplate;

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
        TEMP_TOKEN_TTL,
        TimeUnit.MINUTES
    );

    redirectStrategy.sendRedirect(request, response, targetUrl);
  }

  private String getTargetUrl(String tempToken) {
    return UriComponentsBuilder
        .fromUriString(FRONT_SERVER + LOGIN_REDIRECT_URL)
        .queryParam("success", tempToken)
        .build().toUriString();
  }
}

