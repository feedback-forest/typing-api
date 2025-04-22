package dasi.typing.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtToken {

  private String grantType;

  private String accessToken;

  private String refreshToken;

  @Builder
  private JwtToken(String grantType, String accessToken, String refreshToken) {
    this.grantType = grantType;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
