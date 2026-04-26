package dasi.typing.api.service.ranking;

import static dasi.typing.utils.ConstantUtil.RANKING_MEMBER_KEY;
import static dasi.typing.utils.ConstantUtil.RANKING_MEMBER_MONTHLY_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.RANKING_MONTHLY_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.RANKING_REALTIME_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.api.service.ranking.dto.RankingMemberData;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCacheService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String UPDATE_IF_BETTER_LUA = """
      local cur = redis.call('ZSCORE', KEYS[1], ARGV[1])
      if cur == false or tonumber(cur) < tonumber(ARGV[2]) then
          redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])
          redis.call('HSET', KEYS[2], ARGV[1], ARGV[3])
          return 1
      end
      return 0
      """;

  private final RedisScript<Long> updateIfBetterScript = new DefaultRedisScript<>(UPDATE_IF_BETTER_LUA, Long.class);

  public static double toCompositeScore(int score, int maxCpm, double acc) {
    return score * 1_000_000_000.0
           + maxCpm * 10_000.0
           + (int) (acc * 10000);
  }

  public void warmupRealtime(List<Object[]> bestRecords) {
    redisTemplate.delete(RANKING_REALTIME_KEY);
    redisTemplate.delete(RANKING_MEMBER_KEY);

    for (Object[] record : bestRecords) {
      Long memberId = ((Number) record[0]).longValue();
      String nickname = (String) record[1];
      int score = ((Number) record[2]).intValue();
      int maxCpm = ((Number) record[3]).intValue();
      double acc = ((Number) record[4]).doubleValue();
      LocalDateTime createdDate = convertToLocalDateTime(record[5]);

      double compositeScore = toCompositeScore(score, maxCpm, acc);
      String memberKey = String.valueOf(memberId);

      redisTemplate.opsForZSet().add(RANKING_REALTIME_KEY, memberKey, compositeScore);
      redisTemplate.opsForHash().put(RANKING_MEMBER_KEY, memberKey,
          toJson(memberId, nickname, score, createdDate));
    }

    log.info("[Ranking] 실시간 랭킹 Warmup 완료 - {} 명", bestRecords.size());
  }

  public void warmupMonthly(List<Object[]> bestRecords, YearMonth targetMonth) {
    String monthlyKey = RANKING_MONTHLY_KEY_PREFIX + targetMonth;
    String monthlyMemberKey = RANKING_MEMBER_MONTHLY_KEY_PREFIX + targetMonth;

    redisTemplate.delete(monthlyKey);
    redisTemplate.delete(monthlyMemberKey);

    LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
    LocalDateTime monthEnd = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

    for (Object[] record : bestRecords) {
      LocalDateTime createdDate = convertToLocalDateTime(record[5]);

      if (createdDate.isBefore(monthStart) || !createdDate.isBefore(monthEnd)) {
        continue;
      }

      Long memberId = ((Number) record[0]).longValue();
      String nickname = (String) record[1];
      int score = ((Number) record[2]).intValue();
      int maxCpm = ((Number) record[3]).intValue();
      double acc = ((Number) record[4]).doubleValue();

      double compositeScore = toCompositeScore(score, maxCpm, acc);
      String memberKey = String.valueOf(memberId);

      redisTemplate.opsForZSet().add(monthlyKey, memberKey, compositeScore);
      redisTemplate.opsForHash().put(monthlyMemberKey, memberKey,
          toJson(memberId, nickname, score, createdDate));
    }

    log.info("[Ranking] 월간 랭킹 Warmup 완료 - month={}", targetMonth);
  }

  public void addOrUpdateIfBetter(Long memberId, String nickname, int score, int maxCpm, double acc, LocalDateTime createdDate) {

    double newComposite = toCompositeScore(score, maxCpm, acc);
    String memberKey = String.valueOf(memberId);

    updateZSetIfBetter(RANKING_REALTIME_KEY, RANKING_MEMBER_KEY, memberKey, newComposite, memberId, nickname, score, createdDate);

    YearMonth currentMonth = YearMonth.now();
    String monthlyKey = RANKING_MONTHLY_KEY_PREFIX + currentMonth;
    String monthlyMemberKey = RANKING_MEMBER_MONTHLY_KEY_PREFIX + currentMonth;

    updateZSetIfBetter(monthlyKey, monthlyMemberKey, memberKey, newComposite, memberId, nickname, score, createdDate);
  }

  public List<RankingResponse> getRealtimeTopN(int count) {
    return getTopN(RANKING_REALTIME_KEY, RANKING_MEMBER_KEY, count);
  }

  public List<RankingResponse> getMonthlyTopN(int count) {
    YearMonth currentMonth = YearMonth.now();
    String monthlyKey = RANKING_MONTHLY_KEY_PREFIX + currentMonth;
    String monthlyMemberKey = RANKING_MEMBER_MONTHLY_KEY_PREFIX + currentMonth;

    return getTopN(monthlyKey, monthlyMemberKey, count);
  }

  public Long getMemberRealtimeRank(Long memberId) {
    Long rank = redisTemplate.opsForZSet()
        .reverseRank(RANKING_REALTIME_KEY, String.valueOf(memberId));
    return rank != null ? rank + 1 : null;
  }

  private void updateZSetIfBetter(String zsetKey, String hashKey,
      String memberKey, double newComposite,
      Long memberId, String nickname, int score, LocalDateTime createdDate) {

    String json = toJson(memberId, nickname, score, createdDate);
    redisTemplate.execute(
        updateIfBetterScript,
        List.of(zsetKey, hashKey),
        memberKey,
        String.valueOf(newComposite),
        json
    );
  }

  private List<RankingResponse> getTopN(String zsetKey, String hashKey, int count) {
    Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(zsetKey, 0, count - 1L);

    if (tuples == null || tuples.isEmpty()) {
      return List.of();
    }

    List<RankingResponse> responses = new ArrayList<>();
    long ranking = 1;

    for (TypedTuple<String> tuple : tuples) {
      String memberKey = tuple.getValue();
      if (memberKey == null) {
        continue;
      }

      Object jsonObj = redisTemplate.opsForHash().get(hashKey, memberKey);
      if (jsonObj == null) {
        continue;
      }

      RankingResponse response = fromJson((String) jsonObj, ranking);
      if (response != null) {
        responses.add(response);
        ranking++;
      }
    }

    return responses;
  }

  private String toJson(Long memberId, String nickname, int score, LocalDateTime createdDate) {
    try {
      RankingMemberData data = new RankingMemberData(memberId, nickname, score, createdDate);
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("랭킹 데이터 직렬화 실패: memberId=" + memberId, e);
    }
  }

  private RankingResponse fromJson(String json, long ranking) {
    try {
      RankingMemberData data = objectMapper.readValue(json, RankingMemberData.class);
      return RankingResponse.of(
          data.memberId(),
          data.nickname(),
          data.score(),
          data.createdDate(),
          ranking
      );
    } catch (JsonProcessingException e) {
      log.warn("[Ranking] 랭킹 데이터 역직렬화 실패: {}", json, e);
      return null;
    }
  }

  private LocalDateTime convertToLocalDateTime(Object dateObj) {
    if (dateObj instanceof java.sql.Timestamp timestamp) {
      return timestamp.toLocalDateTime();
    }
    if (dateObj instanceof LocalDateTime localDateTime) {
      return localDateTime;
    }
    throw new IllegalStateException("지원하지 않는 날짜 타입: " + dateObj.getClass());
  }
}