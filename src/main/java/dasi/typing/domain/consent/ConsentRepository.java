package dasi.typing.domain.consent;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {

  List<Consent> findByTypeIn(List<ConsentType> agreements);

  List<Consent> findByTypeInAndActiveTrue(List<ConsentType> types);

  List<Consent> findByActiveTrue();

  Optional<Consent> findByTypeAndActiveTrue(ConsentType type);
}
