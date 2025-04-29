package dasi.typing.api.controller.phrase.response;

import dasi.typing.domain.phrase.Phrase;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PhraseResponse {

  private Long id;
  private String sentence;
  private String title;
  private String author;
  private String lang;
  private String type;

  @Builder
  private PhraseResponse(Long id, String sentence, String title, String author, String lang,
      String type) {
    this.id = id;
    this.sentence = sentence;
    this.title = title;
    this.author = author;
    this.lang = lang;
    this.type = type;
  }

  public static PhraseResponse from(Phrase phrase) {
    return PhraseResponse.builder()
        .id(phrase.getId())
        .sentence(phrase.getSentence())
        .title(phrase.getTitle())
        .author(phrase.getAuthor())
        .lang(phrase.getLang().name())
        .type(phrase.getType().name())
        .build();
  }
}
