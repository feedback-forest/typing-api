package dasi.typing.api.service.ranking;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.typing.TypingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

  private final TypingRepository typingRepository;

  public List<RankingResponse> getRealTimeRanking() {
    return typingRepository.findTop50WithSequentialRank();
  }

  public List<RankingResponse> getMonthlyRanking() {

    LocalDate now = LocalDate.now();
    LocalDateTime startDate = getMonthStartDate(now);
    LocalDateTime endDate = getMonthEndDate(now);

    return typingRepository.findTop50WithMonthlySequentialRank(startDate, endDate);
  }

  private static LocalDateTime getMonthStartDate(LocalDate now) {
    return now.withDayOfMonth(1)
        .atTime(0, 0, 0, 0);
  }

  private static LocalDateTime getMonthEndDate(LocalDate now) {
    return now.with(TemporalAdjusters.lastDayOfMonth())
        .atTime(23, 59, 59, 999999999);
  }
}
