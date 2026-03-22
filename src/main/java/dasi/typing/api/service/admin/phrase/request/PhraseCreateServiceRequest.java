package dasi.typing.api.service.admin.phrase.request;

import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;

public record PhraseCreateServiceRequest(
    String sentence,
    String title,
    String author,
    Lang lang,
    LangType type
) {

}
