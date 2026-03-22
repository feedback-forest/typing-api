package dasi.typing.api.controller.member.request;

import dasi.typing.api.service.member.request.MemberNicknameServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberNicknameRequest {

  private String nickname;

  public MemberNicknameServiceRequest toServiceRequest() {
    return new MemberNicknameServiceRequest(nickname);
  }
}
