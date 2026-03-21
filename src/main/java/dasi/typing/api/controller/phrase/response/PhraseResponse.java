package dasi.typing.api.controller.phrase.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonCreator
  public PhraseResponse(
      @JsonProperty("id") Long id,
      @JsonProperty("sentence") String sentence,
      @JsonProperty("title") String title,
      @JsonProperty("author") String author,
      @JsonProperty("lang") String lang,
      @JsonProperty("type") String type) {
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
