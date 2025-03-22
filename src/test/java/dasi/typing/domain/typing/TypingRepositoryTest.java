package dasi.typing.domain.typing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dasi.typing.api.controller.ranking.response.RankingResponse;
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
class TypingRepositoryTest {

  @Autowired
  private TypingRepository typingRepository;

  @Test
  @DisplayName("충분히 많은 데이터가 존재할 때, 총점 기준으로 내림차순하여 상위 50명의 랭킹 정보를 조회할 수 있다.")
  void findTop50WithSequentialRank() {
    // given
    List<RankingResponse> responses = typingRepository.findTop50WithSequentialRank();

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
  @DisplayName("월별 랭킹 조회를 했을 때, 모든 데이터의 연월은 현재 날짜의 연월과 같다.")
  void findTop50WithMonthlySequentialRank() {
    // given
    LocalDate now = LocalDate.now();
    LocalDateTime startDate = getMonthStartDate(now);
    LocalDateTime endDate = getMonthEndDate(now);

    // when
    List<RankingResponse> responses = typingRepository
        .findTop50WithMonthlySequentialRank(startDate, endDate);

    // then
    assertTrue(responses.size() <= 50);
    for (RankingResponse response : responses) {
      assertThat(response.getCreatedDate().getYear()).isEqualTo(now.getYear());
      assertThat(response.getCreatedDate().getMonthValue()).isEqualTo(now.getMonthValue());
    }
  }

  @Test
  @DisplayName("현재 날짜에 해당하는 연월에 대한 랭킹 조회를 할 수 있다.")
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

      assertTrue(inRange(startDate, current.getCreatedDate(), endDate));
      assertTrue(inRange(startDate, next.getCreatedDate(), endDate));
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

  private boolean inRange(LocalDateTime start, LocalDateTime date, LocalDateTime end) {
    return !date.isBefore(start) && !date.isAfter(end);
  }

}