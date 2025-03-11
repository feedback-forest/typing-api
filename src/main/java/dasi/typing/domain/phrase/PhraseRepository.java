package dasi.typing.domain.phrase;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhraseRepository extends JpaRepository<Phrase, Long> {

  @Query("SELECT p FROM Phrase p ORDER BY FUNCTION('RAND') LIMIT 20")
  List<Phrase> findRandom20Phrases();

}
