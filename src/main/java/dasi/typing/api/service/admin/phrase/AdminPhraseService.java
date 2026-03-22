package dasi.typing.api.service.admin.phrase;

import dasi.typing.api.service.admin.phrase.request.PhraseCreateServiceRequest;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPhraseService {

  private final PhraseRepository phraseRepository;

  @Transactional
  public void createPhrases(List<PhraseCreateServiceRequest> requests) {
    List<Phrase> phrases = requests.stream()
        .map(req -> Phrase.builder()
            .sentence(req.sentence())
            .title(req.title())
            .author(req.author())
            .lang(req.lang())
            .type(req.type())
            .randId(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE))
            .build())
        .toList();

    phraseRepository.saveAll(phrases);
  }
}
