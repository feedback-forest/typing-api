package dasi.typing.api.service.ranking;

import static dasi.typing.utils.ConstantUtil.RANKING_COUNT;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dasi.typing.api.controller.ranking.response.RankingResponse;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RankingService {

  private final RankingCacheService rankingCacheService;
  private final RankingQueryService rankingQueryService;
  private final long redisFailureCooldownMillis;

  private final Cache<String, List<RankingResponse>> fallbackCache;
  private final ConcurrentHashMap<String, ReentrantLock> fallbackLocks = new ConcurrentHashMap<>();
  private final AtomicLong redisBypassUntilMillis = new AtomicLong(0);

  private static final String REALTIME_CACHE_KEY = "realtime";
  private static final String MONTHLY_CACHE_KEY = "monthly";

  public RankingService(
      RankingCacheService rankingCacheService,
      RankingQueryService rankingQueryService,
      @Value("${ranking.redis-failure-cooldown-millis:3000}") long redisFailureCooldownMillis,
      @Value("${ranking.fallback-cache.ttl-seconds:30}") int fallbackTtlSeconds
  ) {
    this.rankingCacheService = rankingCacheService;
    this.rankingQueryService = rankingQueryService;
    this.redisFailureCooldownMillis = redisFailureCooldownMillis;

    this.fallbackCache = Caffeine.newBuilder()
        .maximumSize(10)
        .expireAfterWrite(fallbackTtlSeconds, TimeUnit.SECONDS)
        .build();

    log.info("[Ranking] Redis-primary mode. fallbackTTL={}s, redisCooldown={}ms", fallbackTtlSeconds, redisFailureCooldownMillis);
  }

  public List<RankingResponse> getRealTimeRanking() {
    if (shouldBypassRedis()) {
      return loadRealtimeFallback();
    }

    try {
      List<RankingResponse> redisData = rankingCacheService.getRealtimeTopN(RANKING_COUNT);
      if (!redisData.isEmpty()) {
        clearRedisBypass();
        return redisData;
      }
    } catch (Exception e) {
      markRedisFailure();
      log.warn("[Ranking] Redis 조회 실패. fallback 경로 진입 (realtime)", e);
      return loadRealtimeFallback();
    }

    return rankingQueryService.findRealtimeTopN();
  }

  public List<RankingResponse> getMonthlyRanking() {
    if (shouldBypassRedis()) {
      return loadMonthlyFallback();
    }

    try {
      List<RankingResponse> redisData = rankingCacheService.getMonthlyTopN(RANKING_COUNT);
      if (!redisData.isEmpty()) {
        clearRedisBypass();
        return redisData;
      }
    } catch (Exception e) {
      markRedisFailure();
      log.warn("[Ranking] Redis 조회 실패. fallback 경로 진입 (monthly)", e);
      return loadMonthlyFallback();
    }

    return rankingQueryService.findMonthlyTopN();
  }

  private List<RankingResponse> loadRealtimeFallback() {
    List<RankingResponse> cached = fallbackCache.getIfPresent(REALTIME_CACHE_KEY);
    if (cached != null) {
      return cached;
    }

    ReentrantLock lock = fallbackLocks.computeIfAbsent(REALTIME_CACHE_KEY, k -> new ReentrantLock());
    lock.lock();
    try {
      List<RankingResponse> doubleCheck = fallbackCache.getIfPresent(REALTIME_CACHE_KEY);
      if (doubleCheck != null) {
        return doubleCheck;
      }

      List<RankingResponse> dbData = rankingQueryService.findRealtimeTopN();
      fallbackCache.put(REALTIME_CACHE_KEY, dbData);
      log.info("[Ranking] Caffeine fallback 적재 완료 (realtime)");
      return dbData;
    } finally {
      lock.unlock();
    }
  }

  private List<RankingResponse> loadMonthlyFallback() {
    List<RankingResponse> cached = fallbackCache.getIfPresent(MONTHLY_CACHE_KEY);
    if (cached != null) {
      return cached;
    }

    ReentrantLock lock = fallbackLocks.computeIfAbsent(MONTHLY_CACHE_KEY, k -> new ReentrantLock());
    lock.lock();
    try {
      List<RankingResponse> doubleCheck = fallbackCache.getIfPresent(MONTHLY_CACHE_KEY);
      if (doubleCheck != null) {
        return doubleCheck;
      }

      List<RankingResponse> dbData = rankingQueryService.findMonthlyTopN();
      fallbackCache.put(MONTHLY_CACHE_KEY, dbData);
      log.info("[Ranking] Caffeine fallback 적재 완료 (monthly)");
      return dbData;
    } finally {
      lock.unlock();
    }
  }

  private boolean shouldBypassRedis() {
    return System.currentTimeMillis() < redisBypassUntilMillis.get();
  }

  private void markRedisFailure() {
    redisBypassUntilMillis.set(System.currentTimeMillis() + redisFailureCooldownMillis);
  }

  private void clearRedisBypass() {
    long current = redisBypassUntilMillis.get();
    if (current != 0) {
      redisBypassUntilMillis.set(0);
      fallbackCache.invalidateAll();
      log.info("[Ranking] Redis 복구 감지. fallback 캐시 무효화 완료.");
    }
  }
}
