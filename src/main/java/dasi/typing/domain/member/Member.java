package dasi.typing.domain.member;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import dasi.typing.domain.BaseEntity;
import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentType;
import dasi.typing.domain.memberConsent.MemberConsent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String kakaoId;

  @Column(unique = true)
  private String nickname;

  @Enumerated(STRING)
  private Role role;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "member_id")
  private List<MemberConsent> agreements = new ArrayList<>();

  public Member(String kakaoId, String nickname) {
    this.kakaoId = kakaoId;
    this.nickname = nickname;
    this.role = Role.USER;
  }

  public void addConsent(List<Consent> consents) {
    for (Consent consent : consents) {
      agreements.add(MemberConsent.of(consent));
    }
  }

  public void reConsent(List<Consent> newConsents) {
    Set<ConsentType> types = newConsents.stream()
        .map(Consent::getType)
        .collect(Collectors.toSet());

    agreements.removeIf(mc -> types.contains(mc.getConsent().getType()));

    for (Consent consent : newConsents) {
      agreements.add(MemberConsent.of(consent));
    }
  }
}
