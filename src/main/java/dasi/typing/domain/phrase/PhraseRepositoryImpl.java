package dasi.typing.domain.phrase;

import static dasi.typing.domain.phrase.QPhrase.phrase;
import static java.util.concurrent.ThreadLocalRandom.current;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PhraseRepositoryImpl implements PhraseRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Phrase> getRandomPhrases(int limit) {
    Integer maxRandId = queryFactory
        .select(phrase.randId.max())
        .from(phrase)
        .fetchOne();

    int randomThreshold = current().nextInt(maxRandId + 1);
    List<Phrase> result = queryFactory
        .selectFrom(phrase)
        .where(phrase.randId.goe(randomThreshold))
        .orderBy(phrase.randId.asc())
        .limit(limit)
        .fetch();

    if (result.size() < limit) {
      result = queryFactory
          .selectFrom(phrase)
          .orderBy(phrase.randId.asc())
          .limit(limit)
          .fetch();
    }

    return result;
  }
}
