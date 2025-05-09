package dasi.typing.api.service.typing.request;

import dasi.typing.domain.member.Member;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.typing.Typing;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TypingCreateServiceRequest {

  private Long phraseId;

  private Integer cpm;

  private Integer acc;

  private Integer wpm;

  private Integer maxCpm;

  @Builder
  private TypingCreateServiceRequest(Long phraseId, Integer cpm, Integer acc, Integer wpm,
      Integer maxCpm) {
    this.phraseId = phraseId;
    this.cpm = cpm;
    this.acc = acc;
    this.wpm = wpm;
    this.maxCpm = maxCpm;
  }

  public Typing toEntity(Phrase phrase, Member member) {
    return Typing.builder()
        .member(member)
        .phrase(phrase)
        .cpm(cpm)
        .acc(acc)
        .wpm(wpm)
        .maxCpm(maxCpm).build();
  }
}
