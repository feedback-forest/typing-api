package dasi.typing.domain.typing;

import dasi.typing.domain.BaseEntity;
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
public class Typing extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer cpm;

  private Double acc;

  private Integer wpm;

  private Integer maxCpm;

  private Integer score;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  private Phrase phrase;

  @Builder
  private Typing(Integer cpm, Double acc, Integer wpm, Integer maxCpm, Member member,
      Phrase phrase) {
    this.cpm = cpm;
    this.acc = acc;
    this.wpm = wpm;
    this.maxCpm = maxCpm;
    this.member = member;
    this.phrase = phrase;
    this.score = calculateScore();
  }

  public int calculateScore() {
    double penalty = getPenaltyRate(acc);
    return (int) Math.round(cpm * (1 - penalty));
  }

  private double getPenaltyRate(double accuracy) {
    if (accuracy >= 0.90) return 0.0;
    if (accuracy >= 0.40) return 0.9 - accuracy;
    if (accuracy >= 0.35) return 0.60;
    if (accuracy >= 0.30) return 0.70;
    if (accuracy >= 0.25) return 0.80;
    if (accuracy >= 0.20) return 0.90;
    if (accuracy >= 0.10) return 0.97;
    return 1.0;
  }
}
