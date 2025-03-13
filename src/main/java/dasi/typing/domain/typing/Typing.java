package dasi.typing.domain.typing;

import dasi.typing.domain.member.Member;
import dasi.typing.domain.phrase.Phrase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Typing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer cpm;

  private Integer acc;

  private Integer wpm;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  private Phrase phrase;

  @Builder
  private Typing(Integer cpm, Integer acc, Integer wpm, Member member, Phrase phrase) {
    this.cpm = cpm;
    this.acc = acc;
    this.wpm = wpm;
    this.member = member;
    this.phrase = phrase;
  }
}
