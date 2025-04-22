package dasi.typing.domain.memberConsent;

import dasi.typing.domain.BaseEntity;
import dasi.typing.domain.consent.Consent;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class MemberConsent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consent_id")
  private Consent consent;

  private final boolean agreed = true;

  @Builder
  private MemberConsent(Consent consent) {
    this.consent = consent;
  }

  public static MemberConsent of(Consent consent) {
    return MemberConsent.builder()
        .consent(consent).build();
  }
}
