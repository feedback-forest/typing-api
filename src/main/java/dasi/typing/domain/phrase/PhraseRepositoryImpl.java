package dasi.typing.domain.phrase;

import static dasi.typing.domain.phrase.QPhrase.*;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PhraseRepositoryImpl implements PhraseRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Phrase> getRandomPhrases(int phraseCount) {

    NumberExpression<Double> rand = Expressions.numberTemplate(Double.class, "rand");

    return queryFactory
        .selectFrom(phrase)
        .orderBy(rand.asc())
        .limit(phraseCount)
        .fetch();
  }
}
