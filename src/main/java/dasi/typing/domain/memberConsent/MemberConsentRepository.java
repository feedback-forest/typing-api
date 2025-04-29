package dasi.typing.domain.memberConsent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberConsentRepository extends JpaRepository<MemberConsent, Long> {

}
