package dasi.typing.api.service.phrase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "phrase.warmup.enabled", havingValue = "true", matchIfMissing = true)
public class PhraseWarmupInitializer {

  private final PhraseService phraseService;

  @EventListener(ApplicationReadyEvent.class)
  public void warmup() {
    log.info("[Phrase] 문장 캐시 Warmup 시작");
    phraseService.warmup();
    log.info("[Phrase] 문장 캐시 Warmup 완료");
  }
}
