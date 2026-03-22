package dasi.typing.api.service.phrase;

import static dasi.typing.domain.phrase.Lang.KO;
import static dasi.typing.domain.phrase.LangType.POEM;
import static dasi.typing.utils.ConstantUtil.PHRASE_QUEUE_KEY_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class PhraseServiceTest {

  @Autowired
  private PhraseService phraseService;

  @Autowired
  private PhraseRepository phraseRepository;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    redisTemplate.delete(PHRASE_QUEUE_KEY_PREFIX + LocalDate.now());
    phraseRepository.deleteAllInBatch();
  }

  @AfterEach
  void tearDown() {
    redisTemplate.delete(PHRASE_QUEUE_KEY_PREFIX + LocalDate.now());
    phraseRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("Redis 큐에서 JSON 객체를 직접 조회하여 PhraseResponse 리스트로 반환한다.")
  void getRandomPhrasesFromQueueTest() throws Exception {
    // given
    Phrase phrase = createPhrase("test sentence");
    phraseRepository.save(phrase);

    // PhraseResponse JSON을 Redis에 직접 저장
    PhraseResponse response = PhraseResponse.from(phrase);
    String json = objectMapper.writeValueAsString(response);
    String redisKey = PHRASE_QUEUE_KEY_PREFIX + LocalDate.now();
    redisTemplate.opsForList().rightPush(redisKey, json);

    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).hasSize(1);
    assertThat(responses.getFirst().getSentence()).isEqualTo("test sentence");
  }

  @Test
  @DisplayName("Redis 큐가 없을 경우 fallback으로 DB에서 직접 조회한다.")
  void getRandomPhrasesFallbackTest() {
    // given - Redis 큐를 생성하지 않음 → fallback 발동
    Phrase phrase = createPhrase("fallback sentence");
    phraseRepository.save(phrase);

    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).hasSize(1);
    assertThat(responses.getFirst().getSentence()).isEqualTo("fallback sentence");
  }

  @Test
  @DisplayName("phrase가 없을 경우 빈 리스트를 반환한다.")
  void getRandomPhrasesEmptyTest() {
    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).isEmpty();
  }

  private static Phrase createPhrase(String sentence) {
    return Phrase.builder()
        .sentence(sentence)
        .title("test title")
        .author("test author")
        .lang(KO)
        .type(POEM)
        .randId(1)
        .build();
  }
}
