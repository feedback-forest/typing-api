package dasi.typing.api.service.oauth;

import dasi.typing.api.service.oauth.info.KakaoUserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

    OidcUser oidcUser = super.loadUser(userRequest);

    KakaoUserInfo info = new KakaoUserInfo(
        oidcUser.getSubject(),
        oidcUser.getAttributes().get("nickname").toString()
    );

    return CustomOidcUser.builder()
        .oidcUser(oidcUser)
        .info(info).build();
  }
}
