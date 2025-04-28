package dasi.typing.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String REISSUE_URI = "/api/v1/members/reissue";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = resolveToken(request);
    String requestURI = request.getRequestURI();

    if (StringUtils.isNotEmpty(token) && (isReissueRequest(requestURI) || jwtTokenProvider.validateAccessToken(token))) {
      String kakaoId = jwtTokenProvider.getKakaoId(token);
      List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("USER");

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(kakaoId, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(TOKEN_HEADER);

    if (StringUtils.isNotEmpty(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length());
    }
    return null;
  }

  private boolean isReissueRequest(String requestURI) {
    return requestURI.equals(REISSUE_URI);
  }
}
