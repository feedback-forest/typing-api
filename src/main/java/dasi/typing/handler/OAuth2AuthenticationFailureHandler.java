package dasi.typing.handler;

import static dasi.typing.utils.CommonConstant.LOGIN_REDIRECT_URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Value("${front.server}")
  private String FRONT_SERVER;

  private final RedirectStrategy redirectStrategy;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException, ServletException {

    String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
    String targetUrl = getTargetUrl(errorMessage);

    redirectStrategy.sendRedirect(request, response, targetUrl);
  }

  private String getTargetUrl(String errorMessage) {
    return UriComponentsBuilder
        .fromUriString(FRONT_SERVER + LOGIN_REDIRECT_URL)
        .queryParam("error", errorMessage)
        .build().toUriString();
  }
}

