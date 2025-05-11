package dasi.typing.domain.typing;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TypingRepository extends JpaRepository<Typing, Integer> {

  @Query(value = """
      WITH partitionRowNumber AS (
         SELECT t.member_id    AS memberId,
                m.nickname     AS nickname,
                t.score,
                t.max_cpm      AS maxCpm,
                t.acc,
                t.created_date AS createdDate,
                ROW_NUMBER()
                  OVER (PARTITION BY t.member_id ORDER BY t.score DESC, t.max_cpm DESC, t.acc DESC, t.created_date, t.id) AS prn
         FROM typing t JOIN member m ON m.id = t.member_id), 
            globalRowNumber AS (
                SELECT memberId, nickname, score, maxCpm, acc, createdDate,
                        ROW_NUMBER() OVER (ORDER BY score DESC, maxCpm DESC, acc DESC, createdDate, memberId) AS ranking
                FROM partitionRowNumber
                WHERE prn = 1)
      SELECT memberId, nickname, score, maxCpm, acc, createdDate, ranking
      FROM globalRowNumber
      ORDER BY ranking LIMIT 50;
      """, nativeQuery = true)
  List<RankingResponse> findTop50WithSequentialRank();

  @Query(value = """
      WITH partitionRowNumber AS (
         SELECT t.member_id    AS memberId,
                m.nickname     AS nickname,
                t.score,
                t.max_cpm      AS maxCpm,
                t.acc,
                t.created_date AS createdDate,
                ROW_NUMBER()
                  OVER (PARTITION BY t.member_id ORDER BY t.score DESC, t.max_cpm DESC, t.acc DESC, t.created_date, t.id) AS prn
         FROM typing t JOIN member m ON m.id = t.member_id
         WHERE t.created_date BETWEEN :startDate AND :endDate), 
            globalRowNumber AS (
                SELECT memberId, nickname, score, maxCpm, acc, createdDate,
                        ROW_NUMBER() OVER (ORDER BY score DESC, maxCpm DESC, acc DESC, createdDate, memberId) AS ranking
                FROM partitionRowNumber
                WHERE prn = 1)
      SELECT memberId, nickname, score, maxCpm, acc, createdDate, ranking
      FROM globalRowNumber
      ORDER BY ranking LIMIT 50;
      """, nativeQuery = true)
  List<RankingResponse> findTop50WithMonthlySequentialRank(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  @Query(value = """
      WITH partitionRowNumber AS (
         SELECT t.member_id    AS memberId,
                m.nickname     AS nickname,
                t.score,
                t.max_cpm      AS maxCpm,
                t.acc,
                t.created_date AS createdDate,
                ROW_NUMBER()
                  OVER (PARTITION BY t.member_id ORDER BY t.score DESC, t.max_cpm DESC, t.acc DESC, t.created_date, t.id) AS prn
         FROM typing t JOIN member m ON m.id = t.member_id), 
            globalRowNumber AS (
                SELECT memberId, nickname, score, maxCpm, acc, createdDate,
                        ROW_NUMBER() OVER (ORDER BY score DESC, maxCpm DESC, acc DESC, createdDate, memberId) AS ranking
                FROM partitionRowNumber
                WHERE prn = 1)
      SELECT ranking
      FROM globalRowNumber
      WHERE memberId = :memberId;
      """, nativeQuery = true)
  Long findTypingRank(@Param("memberId") Long memberId);


}