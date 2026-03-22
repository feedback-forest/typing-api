package dasi.typing.api.controller.mypage.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WeeklyScoreResponse {

  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate weekStartDate;
  private final Integer highestScore;

  @Builder
  private WeeklyScoreResponse(LocalDate weekStartDate, Integer highestScore) {
    this.weekStartDate = weekStartDate;
    this.highestScore = highestScore;
  }
}
