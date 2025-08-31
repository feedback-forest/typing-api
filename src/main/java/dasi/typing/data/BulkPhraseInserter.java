package dasi.typing.data;

import dasi.typing.domain.phrase.Phrase;
import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BulkPhraseInserter {

  private final JdbcTemplate jdbcTemplate;

  public void insertBulk(List<Phrase> phrases) {
    String sql = """
        INSERT INTO phrase (sentence, title, author, lang, type, created_date, modified_date)
        VALUES (?, ?, ?, ?, ?, NOW(), NOW())
        """;

    jdbcTemplate.batchUpdate(sql, phrases, phrases.size(),
        (PreparedStatement ps, Phrase phrase) -> {
          ps.setString(1, phrase.getSentence());
          ps.setString(2, phrase.getTitle());
          ps.setString(3, phrase.getAuthor());
          ps.setString(4, phrase.getLang().name());
          ps.setString(5, phrase.getType().name());
        });
  }
}