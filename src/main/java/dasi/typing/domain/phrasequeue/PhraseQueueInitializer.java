package dasi.typing.domain.phrasequeue;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "phrase.queue.init.enabled", havingValue = "true", matchIfMissing = true)
public class PhraseQueueInitializer {

  private final PhraseQueueScheduler phraseQueueScheduler;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    phraseQueueScheduler.initializeIfAbsent();
  }
}