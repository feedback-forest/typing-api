package dasi.typing.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class GuestAuthenticationFilter extends OncePerRequestFilter {

  private final String REDIS_KEY_PREFIX = "auth:temp-token:";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {

      String authHeader = request.getHeader("Authorization");

      if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
        String guestId = StringUtils.isNotEmpty(authHeader) ?
            REDIS_KEY_PREFIX + authHeader : UUID.randomUUID().toString();

        AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
            "guestKey",
            new GuestPrincipal(guestId),
            AuthorityUtils.createAuthorityList("GUEST")
        );

        SecurityContextHolder.getContext().setAuthentication(anonymousToken);
      }
    }

    filterChain.doFilter(request, response);
  }
}
