package dasi.typing.api.service.phrase;

import static dasi.typing.utils.ConstantUtil.PHRASE_COUNT;

import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhraseService {

  private final PhraseRepository phraseRepository;

  public List<PhraseResponse> getRandomPhrases() {
    return phraseRepository.getRandomPhrases(PHRASE_COUNT)
        .stream().map(PhraseResponse::from).toList();
  }
}
