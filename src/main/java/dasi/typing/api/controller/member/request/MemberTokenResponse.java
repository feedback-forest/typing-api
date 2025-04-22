package dasi.typing.api.controller.member.request;

import lombok.Getter;

@Getter
public class MemberTokenResponse {

  private final String accessToken;

  public MemberTokenResponse(String accessToken) {
    this.accessToken = accessToken;
  }

}
