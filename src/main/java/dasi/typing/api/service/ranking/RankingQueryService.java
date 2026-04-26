package dasi.typing.api.service.ranking;

import static dasi.typing.utils.ConstantUtil.RANKING_COUNT;

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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RankingQueryService {

  private final TypingRepository typingRepository;

  public List<RankingResponse> findRealtimeTopN() {
    return typingRepository.findRealtimeTopNWithSequentialRank(RANKING_COUNT);
  }

  public List<RankingResponse> findMonthlyTopN() {
    LocalDate now = LocalDate.now();
    LocalDateTime startDate = DateTimeUtil.getMonthStartDate(now);
    LocalDateTime endDate = DateTimeUtil.getNextMonthStartDate(now);
    return typingRepository.findMonthlyTopNWithSequentialRank(startDate, endDate, RANKING_COUNT);
  }
}
