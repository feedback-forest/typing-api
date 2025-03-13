package dasi.typing.api.service.request;

import dasi.typing.domain.member.Member;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.typing.Typing;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TypingCreateServiceRequest {

  private Long phraseId;

  private Integer cpm;

  private Integer acc;

  private Integer wpm;

  @Builder
  private TypingCreateServiceRequest(Long phraseId, Integer cpm, Integer acc, Integer wpm) {
    this.phraseId = phraseId;
    this.cpm = cpm;
    this.acc = acc;
    this.wpm = wpm;
  }

  public Typing toEntity(Phrase phrase, Member member) {
    return Typing.builder()
        .member(member)
        .phrase(phrase)
        .cpm(cpm)
        .acc(acc)
        .wpm(wpm).build();
  }
}
