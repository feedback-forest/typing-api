package dasi.typing.api.controller.mypage.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;

public record WeeklyScoreResponse(
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate weekStartDate,
    Integer highestScore
) {

  @Builder
  public WeeklyScoreResponse(LocalDate weekStartDate, Integer highestScore) {
    this.weekStartDate = weekStartDate;
    this.highestScore = highestScore;
  }
}
