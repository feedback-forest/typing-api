package dasi.typing.api.service.phrase;

import static dasi.typing.utils.ConstantUtil.PHRASE_COUNT;
import static dasi.typing.utils.ConstantUtil.PHRASE_QUEUE_KEY_PREFIX;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhraseService {

  private final PhraseRepository phraseRepository;
  private final ObjectMapper objectMapper;
  private final RedisTemplate<String, String> redisTemplate;

  public List<PhraseResponse> getRandomPhrases() {
    String redisKey = PHRASE_QUEUE_KEY_PREFIX + LocalDate.now();
    Long totalCount = redisTemplate.opsForList().size(redisKey);

    if (totalCount == null || totalCount == 0) {
      log.warn("[PhraseQueue] Redis 큐가 비어 있습니다. fallback 실행.");
      return fallbackRandomPhrases();
    }

    int limit = (int) Math.min(PHRASE_COUNT, totalCount);
    long maxOffset = Math.max(0, totalCount - limit);
    long offset = ThreadLocalRandom.current().nextLong(maxOffset + 1);

    List<String> jsonStrings = redisTemplate.opsForList()
        .range(redisKey, offset, offset + limit - 1);

    if (jsonStrings == null || jsonStrings.isEmpty()) {
      log.warn("[PhraseQueue] Redis LRANGE 결과가 비어 있습니다. fallback 실행.");
      return fallbackRandomPhrases();
    }
    
    return jsonStrings.stream()
        .map(this::fromJson)
        .toList();
  }

  private List<PhraseResponse> fallbackRandomPhrases() {
    List<Phrase> allPhrases = phraseRepository.findAll();

    if (allPhrases.isEmpty()) {
      return List.of();
    }

    List<PhraseResponse> responses = new ArrayList<>(
        allPhrases.stream().map(PhraseResponse::from).toList()
    );
    Collections.shuffle(responses);
    return responses.subList(0, Math.min(PHRASE_COUNT, responses.size()));
  }

  private PhraseResponse fromJson(String json) {
    try {
      return objectMapper.readValue(json, PhraseResponse.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("PhraseResponse 역직렬화 실패: " + json, e);
    }
  }
}
