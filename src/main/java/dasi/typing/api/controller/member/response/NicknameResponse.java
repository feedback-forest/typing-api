package dasi.typing.api.controller.member.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NicknameResponse {

  private String nickname;

  @Builder
  private NicknameResponse(String nickname) {
    this.nickname = nickname;
  }
}
