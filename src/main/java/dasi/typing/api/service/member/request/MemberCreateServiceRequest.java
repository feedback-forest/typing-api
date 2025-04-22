package dasi.typing.api.service.member.request;

import dasi.typing.domain.consent.ConsentType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MemberCreateServiceRequest {

  private String nickname;

  private List<ConsentType> agreements;

  @Builder
  private MemberCreateServiceRequest(String nickname, List<ConsentType> agreements) {
    this.nickname = nickname;
    this.agreements = agreements;
  }
}
