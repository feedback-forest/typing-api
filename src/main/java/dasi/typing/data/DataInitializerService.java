package dasi.typing.data;

import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;
import dasi.typing.domain.phrase.Phrase;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializerService {

  private final BulkPhraseInserter bulkInserter;

  public void initializeData() {
    log.info("Starting insertion of 10 million records...");

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      insertWithMultiThread();
    } catch (Exception e) {
      log.error("Bulk insertion failed", e);
      throw new RuntimeException("Failed to insert bulk data", e);
    } finally {
      stopWatch.stop();
      log.info("Bulk insertion completed in {} minutes",
          stopWatch.getTotalTimeSeconds() / 60);
    }

  }

  private void insertWithMultiThread() throws InterruptedException {
    int numberOfThreads = Runtime.getRuntime().availableProcessors();
    int recordsPerThread = 10_000_000 / numberOfThreads;

    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    for (int threadIndex = 0; threadIndex < numberOfThreads; threadIndex++) {
      final int startIndex = threadIndex * recordsPerThread;
      final int endIndex = (threadIndex == numberOfThreads - 1)
          ? 10_000_000
          : (threadIndex + 1) * recordsPerThread;

      executor.submit(() -> {
        try {
          insertRange(startIndex, endIndex);
        } catch (Exception e) {
          log.error("Thread {} failed", Thread.currentThread().getName(), e);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();
  }

  private void insertRange(int startIndex, int endIndex) {
    int batchSize = 5000;

    for (int i = startIndex; i < endIndex; i += batchSize) {
      int actualBatchSize = Math.min(batchSize, endIndex - i);
      List<Phrase> batch = generateBatch(i, actualBatchSize);

      bulkInserter.insertBulk(batch);
    }
  }

  private List<Phrase> generateBatch(int startIndex, int size) {
    return IntStream.range(startIndex, startIndex + size)
        .mapToObj(this::createPhrase)
        .toList();
  }

  private Phrase createPhrase(int index) {
    return Phrase.builder()
        .sentence(generateRandomSentence(index))
        .title("Title " + index)
        .author("Author " + (index % 1000))
        .lang(Lang.values()[index % Lang.values().length])
        .type(LangType.values()[index % LangType.values().length])
        .build();
  }

  private String generateRandomSentence(int index) {
    String[] templates = {
        "The quick brown fox jumps over the lazy dog number %d",
        "Lorem ipsum dolor sit amet consectetur adipiscing elit %d",
        "Spring Boot makes Java development easier and faster %d",
        "Database optimization is crucial for application performance %d"
    };

    return String.format(templates[index % templates.length], index);
  }
}
