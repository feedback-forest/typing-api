package dasi.typing.api.service.typing.response;

import dasi.typing.domain.member.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TypingResponse {

  private Role role;

  private String nickname;

  private Integer rank;

  private String luckyMessage;

  @Builder
  private TypingResponse(Role role, String nickname, Integer rank, String luckyMessage) {
    this.role = role;
    this.nickname = nickname;
    this.rank = rank;
    this.luckyMessage = luckyMessage;
  }
}
