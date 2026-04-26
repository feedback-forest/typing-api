package dasi.typing.api.service.phrase;

import static dasi.typing.domain.phrase.Lang.KO;
import static dasi.typing.domain.phrase.LangType.POEM;
import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PhraseServiceTest {

  @Autowired
  private PhraseService phraseService;

  @Autowired
  private PhraseRepository phraseRepository;

  @BeforeEach
  void setUp() {
    phraseRepository.deleteAllInBatch();
  }

  @AfterEach
  void tearDown() {
    phraseRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("Caffeine 캐시에서 문장을 조회하여 PhraseResponse 리스트로 반환한다.")
  void getRandomPhrasesFromCacheTest() {
    // given
    Phrase phrase = createPhrase("test sentence");
    phraseRepository.save(phrase);

    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).hasSize(1);
    assertThat(responses.getFirst().getSentence()).isEqualTo("test sentence");
  }

  @Test
  @DisplayName("phrase가 없을 경우 빈 리스트를 반환한다.")
  void getRandomPhrasesEmptyTest() {
    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).isEmpty();
  }

  @Test
  @DisplayName("Fisher-Yates 알고리즘으로 반환된 문장들은 중복이 없다.")
  void getRandomPhrasesNoDuplicateTest() {
    // given
    for (int i = 0; i < 30; i++) {
      phraseRepository.save(createPhrase("sentence " + i));
    }

    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).hasSize(20);
    Set<Long> ids = responses.stream()
        .map(PhraseResponse::getId)
        .collect(Collectors.toSet());
    assertThat(ids).hasSize(20);
  }

  @Test
  @DisplayName("문장 수가 요청 수보다 적으면 전체 문장을 반환한다.")
  void getRandomPhrasesLessThanCountTest() {
    // given
    for (int i = 0; i < 5; i++) {
      phraseRepository.save(createPhrase("sentence " + i));
    }

    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses).hasSize(5);
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
