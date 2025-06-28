package dasi.typing.domain.phrase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PhraseRepositoryTest {

  @Autowired
  private PhraseRepository phraseRepository;

  @AfterEach
  void tearDown() {
    phraseRepository.deleteAllInBatch();
  }

  private static Stream<Arguments> findRandomPhraseScenarios() {
    return Stream.of(
        Arguments.of(1, 1),
        Arguments.of(3, 3),
        Arguments.of(5, 5),
        Arguments.of(10, 10),
        Arguments.of(15, 15),
        Arguments.of(20, 20),
        Arguments.of(25, 25)
    );
  }

  @ParameterizedTest
  @MethodSource("findRandomPhraseScenarios")
  @DisplayName("파라미터로 주어진 문장의 개수만큼 조회할 수 있다.")
  void getRandomPhraseTest(int phraseCount, int expectedSize) {
    // given
    List<Phrase> phrases = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      phrases.add(createPhrase(String.valueOf(i), "문장 " + i, "작가 " + i));
    }
    phraseRepository.saveAll(phrases);

    // when
    List<Long> allIds = phrases.stream().map(Phrase::getId).toList();

    List<Long> firstResult = phraseRepository.getRandomPhrases(phraseCount)
        .stream().map(Phrase::getId).toList();

    List<Long> secondResult = phraseRepository.getRandomPhrases(phraseCount)
        .stream().map(Phrase::getId).toList();

    // then
    assertThat(allIds)
        .containsAll(firstResult)
        .containsAll(secondResult);

    assertThat(firstResult)
        .hasSize(expectedSize)
        .hasSameSizeAs(secondResult)
        .isNotEqualTo(secondResult);
  }

  private Phrase createPhrase(String sentence, String title, String author) {
    return Phrase.builder()
        .sentence(sentence)
        .title(title)
        .author(author)
        .lang(Lang.KO)
        .type(LangType.QUOTE)
        .build();
  }
}