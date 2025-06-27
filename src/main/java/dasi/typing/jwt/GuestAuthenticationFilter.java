package dasi.typing.jwt;

import static dasi.typing.utils.ConstantUtil.BEARER_PREFIX;
import static dasi.typing.utils.ConstantUtil.REDIS_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.TOKEN_HEADER;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.startsWith;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class GuestAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {

      String authHeader = request.getHeader(TOKEN_HEADER);

      if (isEmpty(authHeader) || !startsWith(authHeader, BEARER_PREFIX)) {
        String guestId = isNotEmpty(authHeader) ?
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
