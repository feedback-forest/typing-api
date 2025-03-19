package dasi.typing.domain.typing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
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

}