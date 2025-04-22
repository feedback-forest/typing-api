package dasi.typing.api.service.member.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberNicknameServiceRequest {

  private String nickname;

  @Builder
  private MemberNicknameServiceRequest(String nickname) {
    this.nickname = nickname;
  }
}
