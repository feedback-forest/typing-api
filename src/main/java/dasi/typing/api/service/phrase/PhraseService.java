package dasi.typing.api.service.phrase;

import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseService {

  private final PhraseRepository phraseRepository;

  public List<PhraseResponse> getRandomPhrases() {
    return phraseRepository.getRandom20Phrases()
        .stream().map(PhraseResponse::from).toList();
  }
}
