package dasi.typing.api.controller.member.request;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.domain.consent.ConsentType;
import java.util.List;
import lombok.Getter;

@Getter
public class MemberCreateRequest {

  private String nickname;

  private List<ConsentType> agreements;

  public MemberCreateServiceRequest toServiceRequest() {
    return MemberCreateServiceRequest.builder()
        .nickname(nickname)
        .agreements(agreements).build();
  }

}
