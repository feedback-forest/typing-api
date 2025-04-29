package dasi.typing.domain.member;

import dasi.typing.domain.BaseEntity;
import dasi.typing.domain.memberConsent.MemberConsent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String kakaoId;

  @Column(unique = true)
  private String nickname;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "member_id")
  private List<MemberConsent> agreements = new ArrayList<>();

  @Builder
  private Member(String kakaoId, String nickname) {
    this.kakaoId = kakaoId;
    this.nickname = nickname;
  }

  public void addConsent(MemberConsent memberConsent) {
    agreements.add(memberConsent);
  }
}
