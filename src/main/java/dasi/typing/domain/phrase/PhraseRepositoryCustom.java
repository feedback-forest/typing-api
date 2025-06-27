package dasi.typing.domain.phrase;

import java.util.List;

public interface PhraseRepositoryCustom {

  List<Phrase> getRandomPhrases(int phraseCount);

}
