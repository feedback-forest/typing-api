package dasi.typing.domain.phrasequeue;

import static dasi.typing.utils.ConstantUtil.PHRASE_QUEUE_KEY_PREFIX;
import static dasi.typing.utils.ConstantUtil.PHRASE_QUEUE_TTL_HOURS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhraseQueueScheduler {

  private final PhraseRepository phraseRepository;
  private final ObjectMapper objectMapper;
  private final RedisTemplate<String, String> redisTemplate;

  @Scheduled(cron = "0 0 3 * * *")
  public void generateDailyQueue() {
    String redisKey = PHRASE_QUEUE_KEY_PREFIX + LocalDate.now();

    if (redisTemplate.hasKey(redisKey)) {
      log.info("[PhraseQueue] 오늘의 큐가 이미 존재합니다. 스킵합니다. key={}", redisKey);
      return;
    }

    log.info("[PhraseQueue] 배치 시작 - key={}", redisKey);

    List<Phrase> allPhrases = phraseRepository.findAll();
    if (allPhrases.isEmpty()) {
      log.warn("[PhraseQueue] phrase 데이터가 없습니다. 배치를 종료합니다.");
      return;
    }

    List<String> shuffledJsons = new ArrayList<>(
        allPhrases.stream()
            .map(PhraseResponse::from)
            .map(this::toJson)
            .toList()
    );
    Collections.shuffle(shuffledJsons);

    redisTemplate.opsForList().rightPushAll(redisKey, shuffledJsons);
    redisTemplate.expire(redisKey, Duration.ofHours(PHRASE_QUEUE_TTL_HOURS));

    log.info("[PhraseQueue] 배치 완료 - {} 개 문장 JSON 저장, TTL={}h",
        shuffledJsons.size(), PHRASE_QUEUE_TTL_HOURS);
  }

  public void initializeIfAbsent() {
    String redisKey = PHRASE_QUEUE_KEY_PREFIX + LocalDate.now();
    if (!redisTemplate.hasKey(redisKey)) {
      log.info("[PhraseQueue] 오늘의 큐가 없습니다. 초기 생성을 시작합니다.");
      generateDailyQueue();
    }
  }

  private String toJson(PhraseResponse response) {
    try {
      return objectMapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("PhraseResponse 직렬화 실패: " + response.getId(), e);
    }
  }
}