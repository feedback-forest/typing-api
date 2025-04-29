package dasi.typing.api.controller.typing.request;


import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import lombok.Builder;

public class TypingCreateRequest {

  private Long phraseId;

  private Integer cpm;

  private Integer acc;

  private Integer wpm;

  private Integer maxCpm;

  @Builder
  private TypingCreateRequest(Long phraseId, Integer cpm, Integer acc, Integer wpm, Integer maxCpm) {
    this.phraseId = phraseId;
    this.cpm = cpm;
    this.acc = acc;
    this.wpm = wpm;
    this.maxCpm = maxCpm;
  }

  public TypingCreateServiceRequest toServiceRequest() {
    return TypingCreateServiceRequest.builder()
        .phraseId(phraseId)
        .cpm(cpm)
        .acc(acc)
        .wpm(wpm)
        .maxCpm(maxCpm).build();
  }
}