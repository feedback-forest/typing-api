package dasi.typing.domain.phrase;

import dasi.typing.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Phrase extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sentence;

  private String title;

  private String author;

  @Enumerated(EnumType.STRING)
  private Lang lang;

  @Enumerated(EnumType.STRING)
  private LangType type;

  @Builder
  private Phrase(String sentence, String title, String author, Lang lang, LangType type) {
    this.sentence = sentence;
    this.title = title;
    this.author = author;
    this.lang = lang;
    this.type = type;
  }

}
