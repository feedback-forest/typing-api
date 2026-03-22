package dasi.typing.api.service.ranking;

import dasi.typing.domain.typing.TypingRepository;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ranking.warmup.enabled", havingValue = "true", matchIfMissing = true)
public class RankingWarmupInitializer {

  private final TypingRepository typingRepository;
  private final RankingCacheService rankingCacheService;

  @EventListener(ApplicationReadyEvent.class)
  public void warmup() {
    log.info("[Ranking] 랭킹 Warmup 시작");

    List<Object[]> bestRecords = typingRepository.findAllBestRecordsForWarmup();

    if (bestRecords.isEmpty()) {
      log.info("[Ranking] 타이핑 데이터가 없습니다. Warmup을 스킵합니다.");
      return;
    }

    rankingCacheService.warmupRealtime(bestRecords);
    rankingCacheService.warmupMonthly(bestRecords, YearMonth.now());

    log.info("[Ranking] 랭킹 Warmup 완료 - 총 {} 명의 최고 기록 적재", bestRecords.size());
  }
}
