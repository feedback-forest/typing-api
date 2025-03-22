package dasi.typing.api.service.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.typing.TypingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/ranking.sql")
class RankingServiceTest {

  @Autowired
  private RankingService rankingService;

  @Autowired
  private TypingRepository typingRepository;

  @Test
  @DisplayName("충분한 데이터가 존재할 때, 점수를 기준으로 내림차순하여 상위 50명의 데이터를 반환할 수 있다.")
  void getRealTimeRanking() {
    // given
    List<RankingResponse> responses = rankingService.getRealTimeRanking();

    // when
    RankingResponse first = responses.getFirst();
    RankingResponse last = responses.getLast();

    // then
    assertThat(responses).hasSize(50);
    assertEquals(1L, first.getRanking());
    assertEquals(50L, last.getRanking());

    for (int i = 0; i < responses.size() - 1; i++) {
      RankingResponse current = responses.get(i);
      RankingResponse next = responses.get(i + 1);

      assertTrue(current.getScore() >= next.getScore());
      assertEquals(current.getRanking() + 1, next.getRanking());
    }
  }

  @Test
  @DisplayName("현재 날짜에 해당하는 연월에 대해서 최대 50등까지 랭킹 조회를 할 수 있다.")
  void getMonthlyRanking() {
    // given
    LocalDate now = LocalDate.now();
    LocalDateTime startDate = getMonthStartDate(now);
    LocalDateTime endDate = getMonthEndDate(now);

    // when
    List<RankingResponse> responses = typingRepository
        .findTop50WithMonthlySequentialRank(startDate, endDate);

    // then
    assertTrue(responses.size() <= 50);
    for (int i = 0; i < responses.size() - 1; i++) {
      RankingResponse current = responses.get(i);
      RankingResponse next = responses.get(i + 1);

      assertTrue(current.getScore() >= next.getScore());
      assertEquals(current.getRanking() + 1, next.getRanking());
    }
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