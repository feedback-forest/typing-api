package dasi.typing.api.service.typing.response;

import lombok.Builder;

public class TypingResponse {

  private Integer rank;
  private String luckyMessage;

  @Builder
  private TypingResponse(Integer rank, String luckyMessage) {
    this.rank = rank;
    this.luckyMessage = luckyMessage;
  }
}
