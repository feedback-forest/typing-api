package dasi.typing.api.service.member.request;

import dasi.typing.domain.consent.ConsentType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberCreateServiceRequest {

  private final String nickname;

  private final List<ConsentType> agreements;

  @Builder
  private MemberCreateServiceRequest(String nickname, List<ConsentType> agreements) {
    this.nickname = nickname;
    this.agreements = agreements;
  }
}
