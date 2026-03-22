package dasi.typing.handler;

import static dasi.typing.utils.ConstantUtil.REDIS_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.SIGNUP_REDIRECT_URL;
import static dasi.typing.utils.ConstantUtil.TEMP_TOKEN_TTL;

import dasi.typing.api.service.oauth.CustomOidcUser;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import dasi.typing.utils.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
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
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    CustomOidcUser customUser = (CustomOidcUser) authentication.getPrincipal();
    String kakaoId = customUser.getInfo().getSub();

    if (memberRepository.existsByKakaoId(kakaoId)) {
      handleExistingMember(request, response, kakaoId);
    } else {
      handleNewMember(request, response, kakaoId);
    }
  }

  private void handleExistingMember(
      HttpServletRequest request,
      HttpServletResponse response,
      String kakaoId
  ) throws IOException {
    Member member = memberRepository.findByKakaoId(kakaoId)
        .orElseThrow(() -> new CustomException(Code.NOT_EXIST_MEMBER));
    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId, member.getRole(), new Date());
    CookieUtil.addTokenCookies(response, jwtToken);
    redirectStrategy.sendRedirect(request, response, FRONT_SERVER + "/login/callback");
  }

  private void handleNewMember(
      HttpServletRequest request,
      HttpServletResponse response,
      String kakaoId
  ) throws IOException {
    String tempToken = UUID.randomUUID().toString();
    String redisKey = REDIS_KEY_PREFIX + tempToken;

    redisTemplate.opsForValue().set(
        redisKey,
        kakaoId,
        TEMP_TOKEN_TTL,
        TimeUnit.MINUTES
    );

    String targetUrl = UriComponentsBuilder
        .fromUriString(FRONT_SERVER + SIGNUP_REDIRECT_URL)
        .queryParam("tempToken", tempToken)
        .build().toUriString();

    redirectStrategy.sendRedirect(request, response, targetUrl);
  }
}
