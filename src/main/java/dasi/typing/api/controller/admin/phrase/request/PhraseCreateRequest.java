package dasi.typing.api.controller.admin.phrase.request;

import dasi.typing.api.service.admin.phrase.request.PhraseCreateServiceRequest;
import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhraseCreateRequest {

  private String sentence;
  private String title;
  private String author;
  private Lang lang;
  private LangType type;

  public PhraseCreateServiceRequest toServiceRequest() {
    return new PhraseCreateServiceRequest(sentence, title, author, lang, type);
  }
}
