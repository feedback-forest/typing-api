package dasi.typing.api.service.ranking;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.utils.DateTimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

  private static final int RANKING_COUNT = 50;

  private final TypingRepository typingRepository;

  public List<RankingResponse> getRealTimeRanking() {
    return typingRepository.findRealtimeTopNWithSequentialRank(RANKING_COUNT);
  }

  public List<RankingResponse> getMonthlyRanking() {

    LocalDate now = LocalDate.now();
    LocalDateTime startDate = DateTimeUtil.getMonthStartDate(now);
    LocalDateTime endDate = DateTimeUtil.getNextMonthStartDate(now);

    return typingRepository.findMonthlyTopNWithSequentialRank(startDate, endDate, RANKING_COUNT);
  }
}
