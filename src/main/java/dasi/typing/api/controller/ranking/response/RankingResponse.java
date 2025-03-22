package dasi.typing.api.controller.ranking.response;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

  private LocalDateTime createdDate;

  private Long ranking;

  @Builder
  private RankingResponse(Long memberId, String nickname, Integer score, Timestamp createdDate,
      Long ranking) {
    this.memberId = memberId;
    this.nickname = nickname;
    this.score = score;
    this.createdDate = createdDate.toLocalDateTime();
    this.ranking = ranking;
  }
}
