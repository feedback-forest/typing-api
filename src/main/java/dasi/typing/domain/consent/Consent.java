package dasi.typing.domain.consent;

import static lombok.AccessLevel.PROTECTED;

import dasi.typing.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Consent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private ConsentType type;

  private String description;

  private Integer version;

  @Column(columnDefinition = "TEXT")
  private String content;

  private boolean active;

  public Consent(ConsentType type) {
    this.type = type;
    this.description = type.getText();
    this.version = 1;
    this.content = type.getText();
    this.active = true;
  }

  public Consent(ConsentType type, Integer version, String content) {
    this.type = type;
    this.description = type.getText();
    this.version = version;
    this.content = content;
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}