package dasi.typing.domain.typing;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TypingRepository extends JpaRepository<Typing, Integer> {

  @Query(value = """
      SELECT t.member_id AS memberId, 
             m.nickname AS nickname,
             t.wpm AS score,
             ROW_NUMBER() OVER (ORDER BY t.wpm DESC, t.max_wpm DESC, t.acc DESC, t.id) AS ranking
      FROM typing t
      JOIN member m ON t.member_id = m.id
      ORDER BY ranking ASC
      LIMIT 50
      """, nativeQuery = true)
  List<RankingResponse> findTop50WithSequentialRank();

}