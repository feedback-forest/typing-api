package dasi.typing.api.controller.typing.request;


import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TypingCreateRequest {

  private Long phraseId;

  private Integer cpm;

  private double acc;

  private Integer wpm;

  private Integer maxCpm;

  public TypingCreateServiceRequest toServiceRequest() {
    return TypingCreateServiceRequest.builder()
        .phraseId(phraseId)
        .cpm(cpm)
        .acc(acc)
        .wpm(wpm)
        .maxCpm(maxCpm).build();
  }
}