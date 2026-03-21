package dasi.typing.filter;

import static dasi.typing.utils.ConstantUtil.ACCESS_TOKEN_COOKIE;
import static dasi.typing.utils.ConstantUtil.BEARER_PREFIX;
import static dasi.typing.utils.ConstantUtil.REISSUE_URI;
import static dasi.typing.utils.ConstantUtil.TOKEN_HEADER;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import dasi.typing.jwt.JwtTokenProvider;
import dasi.typing.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String token = resolveToken(request);
    String requestURI = request.getRequestURI();

    if (isNotEmpty(token) && (isReissueRequest(requestURI) || jwtTokenProvider.validateAccessToken(token))) {

      String kakaoId = jwtTokenProvider.getKakaoId(token);
      List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("USER");

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(kakaoId, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  public String resolveToken(HttpServletRequest request) {
    String cookieToken = CookieUtil.resolveTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
    if (isNotEmpty(cookieToken)) {
      return cookieToken;
    }

    String bearerToken = request.getHeader(TOKEN_HEADER);
    if (isNotEmpty(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  private boolean isReissueRequest(String requestURI) {
    return requestURI.equals(REISSUE_URI);
  }
}
