package dasi.typing.domain.phrase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhraseRepositoryTest {

  @Autowired
  private PhraseRepository phraseRepository;

  @AfterEach
  void tearDown() {
    phraseRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("랜덤으로 20개의 문장들을 조회할 수 있다.")
  void findRandom20Phrase() {
    // given
    List<Phrase> phrases = new ArrayList<>();
    for (int i = 1; i <= 25; i++) {
      phrases.add(createPhrase(String.valueOf(i), "문장 " + i, "작가 " + i));
    }
    phraseRepository.saveAll(phrases);

    // when
    List<Long> allIds = phrases.stream().map(Phrase::getId).toList();

    List<Long> firstResult = phraseRepository.getRandom20Phrases()
        .stream().map(Phrase::getId).toList();

    List<Long> secondResult = phraseRepository.getRandom20Phrases()
        .stream().map(Phrase::getId).toList();

    // then
    assertThat(allIds)
        .containsAll(firstResult)
        .containsAll(secondResult);

    assertThat(firstResult)
        .hasSize(20)
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