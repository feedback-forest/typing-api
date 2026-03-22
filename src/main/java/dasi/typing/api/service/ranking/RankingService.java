package dasi.typing.api.service.ranking;

import static dasi.typing.utils.ConstantUtil.RANKING_COUNT;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.utils.DateTimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

  private final TypingRepository typingRepository;
  private final RankingCacheService rankingCacheService;

  public List<RankingResponse> getRealTimeRanking() {
    try {
      List<RankingResponse> cached = rankingCacheService.getRealtimeTopN(RANKING_COUNT);
      if (!cached.isEmpty()) {
        return cached;
      }
    } catch (Exception e) {
      log.warn("[Ranking] Redis 실시간 랭킹 조회 실패. DB fallback 실행.", e);
    }

    return typingRepository.findRealtimeTopNWithSequentialRank(RANKING_COUNT);
  }

  public List<RankingResponse> getMonthlyRanking() {
    try {
      List<RankingResponse> cached = rankingCacheService.getMonthlyTopN(RANKING_COUNT);
      if (!cached.isEmpty()) {
        return cached;
      }
    } catch (Exception e) {
      log.warn("[Ranking] Redis 월간 랭킹 조회 실패. DB fallback 실행.", e);
    }

    LocalDate now = LocalDate.now();
    LocalDateTime startDate = DateTimeUtil.getMonthStartDate(now);
    LocalDateTime endDate = DateTimeUtil.getNextMonthStartDate(now);

    return typingRepository.findMonthlyTopNWithSequentialRank(startDate, endDate, RANKING_COUNT);
  }
}
