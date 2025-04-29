package dasi.typing.domain.typing;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TypingRepository extends JpaRepository<Typing, Integer> {

  @Query(value = """
      SELECT t.member_id AS memberId, 
             m.nickname AS nickname,
             t.score AS score,
             t.created_date,
             ROW_NUMBER() OVER (ORDER BY t.score DESC, t.max_cpm DESC, t.acc DESC, t.created_date, t.id) AS ranking
      FROM typing t
      JOIN member m ON t.member_id = m.id
      ORDER BY ranking ASC
      LIMIT 50
      """, nativeQuery = true)
  List<RankingResponse> findTop50WithSequentialRank();

  @Query(value = """
      SELECT t.member_id AS memberId, 
             m.nickname AS nickname,
             t.score AS score,
             t.created_date,
             ROW_NUMBER() OVER (ORDER BY t.score DESC, t.max_cpm DESC, t.acc DESC, t.created_date, t.id) AS ranking
      FROM typing t
      JOIN member m ON t.member_id = m.id
      WHERE t.created_date BETWEEN :startDate AND :endDate
      ORDER BY ranking ASC
      LIMIT 50
      """, nativeQuery = true)
  List<RankingResponse> findTop50WithMonthlySequentialRank(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  @Query(value = """
      SELECT r.ranking
      FROM (
          SELECT t.id,
                 ROW_NUMBER() OVER (ORDER BY t.score DESC, t.max_cpm DESC, t.acc DESC, t.created_date, t.id) AS ranking
          FROM typing t
      ) r
      WHERE r.id = :typingId
      """, nativeQuery = true)
  int findTypingRank(@Param("typingId") Long typingId);


}