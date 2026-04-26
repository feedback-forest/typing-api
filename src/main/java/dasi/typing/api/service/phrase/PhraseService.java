package dasi.typing.api.service.phrase;

import static dasi.typing.utils.ConstantUtil.PHRASE_COUNT;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PhraseService {

  private final PhraseRepository phraseRepository;
  private final Cache<String, List<PhraseResponse>> phraseCache;

  private static final String CACHE_KEY = "phrases";

  public PhraseService(PhraseRepository phraseRepository, @Value("${phrase.cache.ttl-hours:24}") int ttlHours,
      @Value("${phrase.cache.max-size:1}") int maxSize) {
    this.phraseRepository = phraseRepository;
    this.phraseCache = Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(ttlHours, TimeUnit.HOURS)
        .build();

    log.info("[Phrase] Caffeine cache enabled. TTL={}h, maxSize={}", ttlHours, maxSize);
  }

  public List<PhraseResponse> getRandomPhrases() {
    List<PhraseResponse> allPhrases = phraseCache.get(CACHE_KEY, key -> loadFromDb());

    if (allPhrases == null || allPhrases.isEmpty()) {
      return List.of();
    }

    int count = Math.min(PHRASE_COUNT, allPhrases.size());
    return fisherYatesPartialShuffle(allPhrases, count);
  }

  private List<PhraseResponse> fisherYatesPartialShuffle(List<PhraseResponse> source, int count) {
    List<PhraseResponse> copy = new ArrayList<>(source);
    ThreadLocalRandom random = ThreadLocalRandom.current();

    for (int i = 0; i < count; i++) {
      int j = random.nextInt(i, copy.size());
      PhraseResponse temp = copy.get(i);
      copy.set(i, copy.get(j));
      copy.set(j, temp);
    }

    return List.copyOf(copy.subList(0, count));
  }

  public void evictCache() {
    phraseCache.invalidateAll();
    log.info("[Phrase] Caffeine 캐시 무효화 완료.");
  }

  private List<PhraseResponse> loadFromDb() {
    List<PhraseResponse> phrases = phraseRepository.findAll()
        .stream()
        .map(PhraseResponse::from)
        .toList();

    log.info("[Phrase] DB에서 문장 {}개 로드 완료. Caffeine 캐시 적재.", phrases.size());
    return phrases;
  }
}
