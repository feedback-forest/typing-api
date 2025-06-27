package dasi.typing.api.service.phrase;

import static dasi.typing.domain.phrase.Lang.KO;
import static dasi.typing.domain.phrase.LangType.POEM;
import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PhraseServiceTest {

  @Test
  @DisplayName("DB에서 가져온 Phrase 객체 리스트를 PhraseResponse 객체 리스트로 변환하여 반환한다.")
  void convertPhraseToPhraseResponse(
      @Autowired PhraseService phraseService,
      @Autowired PhraseRepository phraseRepository
  ) {

    // given
    Phrase phrase = createPhrase();
    phraseRepository.save(phrase);

    // when
    List<PhraseResponse> responses = phraseService.getRandomPhrases();

    // then
    assertThat(responses.getFirst()).isInstanceOf(PhraseResponse.class);
  }

  private static Phrase createPhrase() {
    return Phrase.builder()
        .sentence("test sentence")
        .title("test title")
        .author("test author")
        .lang(KO)
        .type(POEM)
        .build();
  }
}