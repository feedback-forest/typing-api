package dasi.typing.api.service.ranking;

import static dasi.typing.utils.ConstantUtil.RANKING_MEMBER_KEY;
import static dasi.typing.utils.ConstantUtil.RANKING_MEMBER_MONTHLY_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.RANKING_MONTHLY_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.RANKING_REALTIME_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RankingCacheServiceTest {

  @Autowired
  private RankingCacheService rankingCacheService;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private final YearMonth currentMonth = YearMonth.now();

  @BeforeEach
  void setUp() {
    cleanupRedis();
  }

  @AfterEach
  void tearDown() {
    cleanupRedis();
  }

  private void cleanupRedis() {
    redisTemplate.delete(RANKING_REALTIME_KEY);
    redisTemplate.delete(RANKING_MEMBER_KEY);
    redisTemplate.delete(RANKING_MONTHLY_KEY_PREFIX + currentMonth);
    redisTemplate.delete(RANKING_MEMBER_MONTHLY_KEY_PREFIX + currentMonth);
  }

  @Test
  @DisplayName("Composite Score를 올바르게 계산한다.")
  void toCompositeScoreTest() {
    // given
    int score = 350;
    int maxCpm = 950;
    double acc = 0.875;

    // when
    double composite = RankingCacheService.toCompositeScore(score, maxCpm, acc);

    // then
    double expected = 350 * 1_000_000_000.0 + 950 * 10_000.0 + (int) (0.875 * 10000);
    assertEquals(expected, composite);
  }

  @Test
  @DisplayName("score가 같을 때 maxCpm이 높은 쪽이 더 높은 Composite Score를 가진다.")
  void compositeScoreTiebreakerMaxCpmTest() {
    // given & when
    double high = RankingCacheService.toCompositeScore(100, 500, 0.90);
    double low = RankingCacheService.toCompositeScore(100, 400, 0.90);

    // then
    assertTrue(high > low);
  }

  @Test
  @DisplayName("score, maxCpm이 같을 때 acc가 높은 쪽이 더 높은 Composite Score를 가진다.")
  void compositeScoreTiebreakerAccTest() {
    // given & when
    double high = RankingCacheService.toCompositeScore(100, 500, 0.95);
    double low = RankingCacheService.toCompositeScore(100, 500, 0.85);

    // then
    assertTrue(high > low);
  }

  @Test
  @DisplayName("실시간 랭킹 Warmup을 수행하면 Redis ZSET과 HASH에 데이터가 적재된다.")
  void warmupRealtimeTest() {
    // given
    List<Object[]> records = createBestRecords(10);

    // when
    rankingCacheService.warmupRealtime(records);

    // then
    Long zsetSize = redisTemplate.opsForZSet().size(RANKING_REALTIME_KEY);
    Long hashSize = redisTemplate.opsForHash().size(RANKING_MEMBER_KEY);

    assertEquals(10, zsetSize);
    assertEquals(10, hashSize);
  }

  @Test
  @DisplayName("월간 랭킹 Warmup 시 해당 월의 데이터만 적재된다.")
  void warmupMonthlyFilterTest() {
    // given
    List<Object[]> records = new ArrayList<>();
    LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(15);
    LocalDateTime lastMonth = thisMonth.minusMonths(1);

    records.add(new Object[]{1L, "이번달유저", 300, 800, 0.95, java.sql.Timestamp.valueOf(thisMonth)});
    records.add(new Object[]{2L, "지난달유저", 400, 900, 0.98, java.sql.Timestamp.valueOf(lastMonth)});

    // when
    rankingCacheService.warmupMonthly(records, currentMonth);

    // then
    String monthlyKey = RANKING_MONTHLY_KEY_PREFIX + currentMonth;
    Long zsetSize = redisTemplate.opsForZSet().size(monthlyKey);
    assertEquals(1, zsetSize);
  }

  @Test
  @DisplayName("실시간 랭킹 Top N 조회 시 점수 내림차순으로 반환된다.")
  void getRealtimeTopNTest() {
    // given
    List<Object[]> records = createBestRecords(5);
    rankingCacheService.warmupRealtime(records);

    // when
    List<RankingResponse> responses = rankingCacheService.getRealtimeTopN(50);

    // then
    assertThat(responses).hasSize(5);
    assertEquals(1L, responses.getFirst().getRanking());
    assertEquals(5L, responses.getLast().getRanking());

    for (int i = 0; i < responses.size() - 1; i++) {
      assertTrue(responses.get(i).getScore() >= responses.get(i + 1).getScore());
    }
  }

  @Test
  @DisplayName("Redis에 데이터가 없으면 빈 리스트를 반환한다.")
  void getRealtimeTopNEmptyTest() {
    // when
    List<RankingResponse> responses = rankingCacheService.getRealtimeTopN(50);

    // then
    assertThat(responses).isEmpty();
  }

  @Test
  @DisplayName("새 타이핑 기록이 기존보다 높으면 Redis를 갱신한다.")
  void addOrUpdateIfBetterUpdateTest() {
    // given
    List<Object[]> records = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    records.add(new Object[]{1L, "테스트유저", 100, 200, 0.90, java.sql.Timestamp.valueOf(now)});
    rankingCacheService.warmupRealtime(records);
    rankingCacheService.warmupMonthly(records, currentMonth);

    // when - 더 높은 점수로 업데이트
    rankingCacheService.addOrUpdateIfBetter(1L, "테스트유저", 200, 400, 0.95, now);

    // then
    List<RankingResponse> responses = rankingCacheService.getRealtimeTopN(50);
    assertThat(responses).hasSize(1);
    assertEquals(200, responses.getFirst().getScore());
  }

  @Test
  @DisplayName("새 타이핑 기록이 기존보다 낮으면 Redis를 갱신하지 않는다.")
  void addOrUpdateIfBetterNoUpdateTest() {
    // given
    List<Object[]> records = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    records.add(new Object[]{1L, "테스트유저", 200, 400, 0.95, java.sql.Timestamp.valueOf(now)});
    rankingCacheService.warmupRealtime(records);

    // when - 더 낮은 점수
    rankingCacheService.addOrUpdateIfBetter(1L, "테스트유저", 100, 200, 0.90, now);

    // then
    List<RankingResponse> responses = rankingCacheService.getRealtimeTopN(50);
    assertThat(responses).hasSize(1);
    assertEquals(200, responses.getFirst().getScore());
  }

  @Test
  @DisplayName("신규 유저의 첫 타이핑 기록은 Redis에 새로 추가된다.")
  void addOrUpdateIfBetterNewMemberTest() {
    // given & when
    LocalDateTime now = LocalDateTime.now();
    rankingCacheService.addOrUpdateIfBetter(99L, "신규유저", 300, 600, 0.92, now);

    // then
    List<RankingResponse> responses = rankingCacheService.getRealtimeTopN(50);
    assertThat(responses).hasSize(1);
    assertEquals(300, responses.getFirst().getScore());
    assertEquals("신규유저", responses.getFirst().getNickname());
  }

  @Test
  @DisplayName("특정 회원의 실시간 랭킹 순위를 조회할 수 있다.")
  void getMemberRealtimeRankTest() {
    // given
    List<Object[]> records = createBestRecords(5);
    rankingCacheService.warmupRealtime(records);

    // when - memberId=5가 가장 높은 score(500)
    Long rank = rankingCacheService.getMemberRealtimeRank(5L);

    // then
    assertNotNull(rank);
    assertEquals(1L, rank);
  }

  @Test
  @DisplayName("존재하지 않는 회원의 랭킹 조회 시 null을 반환한다.")
  void getMemberRealtimeRankNotFoundTest() {
    // given
    List<Object[]> records = createBestRecords(3);
    rankingCacheService.warmupRealtime(records);

    // when
    Long rank = rankingCacheService.getMemberRealtimeRank(999L);

    // then
    assertNull(rank);
  }

  @Test
  @DisplayName("월간 랭킹 Top N 조회가 정상 동작한다.")
  void getMonthlyTopNTest() {
    // given
    List<Object[]> records = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    records.add(new Object[]{1L, "유저1", 300, 800, 0.95, java.sql.Timestamp.valueOf(now)});
    records.add(new Object[]{2L, "유저2", 400, 900, 0.98, java.sql.Timestamp.valueOf(now)});
    rankingCacheService.warmupMonthly(records, currentMonth);

    // when
    List<RankingResponse> responses = rankingCacheService.getMonthlyTopN(50);

    // then
    assertThat(responses).hasSize(2);
    assertEquals(400, responses.getFirst().getScore());
    assertEquals(300, responses.getLast().getScore());
  }

  private List<Object[]> createBestRecords(int count) {
    List<Object[]> records = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    for (int i = 1; i <= count; i++) {
      records.add(new Object[]{
          (long) i,
          "유저" + i,
          i * 100,
          i * 200,
          0.90,
          java.sql.Timestamp.valueOf(now)
      });
    }

    return records;
  }
}
