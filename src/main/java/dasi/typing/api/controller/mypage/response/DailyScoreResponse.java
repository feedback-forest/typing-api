package dasi.typing.api.controller.mypage.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;

public record DailyScoreResponse(
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    Integer highestScore
) {

  @Builder
  public DailyScoreResponse(LocalDate date, Integer highestScore) {
    this.date = date;
    this.highestScore = highestScore;
  }
}
