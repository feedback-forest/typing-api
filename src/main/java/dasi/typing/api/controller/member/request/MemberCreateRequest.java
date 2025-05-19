package dasi.typing.api.controller.member.request;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.domain.consent.ConsentType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberCreateRequest {

  private String nickname;

  private List<ConsentType> agreements;

  public MemberCreateServiceRequest toServiceRequest() {
    return MemberCreateServiceRequest.builder()
        .nickname(nickname)
        .agreements(agreements).build();
  }

}
