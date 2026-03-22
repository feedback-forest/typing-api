package dasi.typing.api.controller.mypage.response;

import java.util.List;
import lombok.Builder;

public record MyPageResponse(
    String nickname,
    long totalTypingCount,
    Integer highestScore,
    Long currentRanking,
    List<TypingHistoryResponse> typingHistories,
    List<DailyScoreResponse> dailyScores
) {

  @Builder
  public MyPageResponse {
  }
}
