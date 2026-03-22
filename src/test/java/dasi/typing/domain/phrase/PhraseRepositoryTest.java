package dasi.typing.domain.phrase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @Test
  @DisplayName("문장을 저장하고 전체 조회할 수 있다.")
  void saveAndFindAllTest() {
    // given
    List<Phrase> phrases = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      phrases.add(createPhrase("문장 " + i));
    }
    phraseRepository.saveAll(phrases);

    // when
    List<Phrase> result = phraseRepository.findAll();

    // then
    assertThat(result).hasSize(5);
  }

  @Test
  @DisplayName("ID로 문장을 조회할 수 있다.")
  void findByIdTest() {
    // given
    Phrase saved = phraseRepository.save(createPhrase("테스트 문장"));

    // when
    Phrase found = phraseRepository.findById(saved.getId()).orElseThrow();

    // then
    assertThat(found.getSentence()).isEqualTo("테스트 문장");
  }

  private int randIdCounter = 1;

  private Phrase createPhrase(String sentence) {
    return Phrase.builder()
        .sentence(sentence)
        .title("제목")
        .author("작가")
        .lang(Lang.KO)
        .type(LangType.QUOTE)
        .randId(randIdCounter++)
        .build();
  }
}
