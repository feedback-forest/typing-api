package dasi.typing.api.service.admin.phrase;

import static dasi.typing.domain.phrase.Lang.EN;
import static dasi.typing.domain.phrase.Lang.KO;
import static dasi.typing.domain.phrase.LangType.POEM;
import static dasi.typing.domain.phrase.LangType.QUOTE;
import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.api.service.admin.phrase.request.PhraseCreateServiceRequest;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminPhraseServiceTest {

  @Autowired
  private AdminPhraseService adminPhraseService;

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
  @DisplayName("문장을 일괄 등록한다.")
  void createPhrasesTest() {
    // given
    List<PhraseCreateServiceRequest> requests = List.of(
        new PhraseCreateServiceRequest("힘내세요", "희망", "작가1", KO, POEM),
        new PhraseCreateServiceRequest("Keep going", "Motivation", "Author1", EN, QUOTE)
    );

    // when
    adminPhraseService.createPhrases(requests);

    // then
    List<Phrase> phrases = phraseRepository.findAll();
    assertThat(phrases).hasSize(2);
    assertThat(phrases).extracting("sentence")
        .containsExactlyInAnyOrder("힘내세요", "Keep going");
  }

  @Test
  @DisplayName("등록된 문장에는 랜덤 ID가 할당된다.")
  void createPhrasesWithRandIdTest() {
    // given
    List<PhraseCreateServiceRequest> requests = List.of(
        new PhraseCreateServiceRequest("문장1", "제목1", "작가1", KO, POEM)
    );

    // when
    adminPhraseService.createPhrases(requests);

    // then
    Phrase phrase = phraseRepository.findAll().getFirst();
    assertThat(phrase.getRandId()).isNotNull();
    assertThat(phrase.getRandId()).isGreaterThanOrEqualTo(0);
  }

  @Test
  @DisplayName("빈 리스트로 요청하면 문장이 등록되지 않는다.")
  void createPhrasesEmptyTest() {
    // when
    adminPhraseService.createPhrases(List.of());

    // then
    assertThat(phraseRepository.findAll()).isEmpty();
  }
}
