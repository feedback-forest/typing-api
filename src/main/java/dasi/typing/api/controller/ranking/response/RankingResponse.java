package dasi.typing.api.controller.ranking.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingResponse {

  private Long memberId;

  private String nickname;

  private Integer score;

  private Long ranking;

  @Builder
  private RankingResponse(Long memberId, String nickname, Integer score, Long ranking) {
    this.memberId = memberId;
    this.nickname = nickname;
    this.score = score;
    this.ranking = ranking;
  }
}
