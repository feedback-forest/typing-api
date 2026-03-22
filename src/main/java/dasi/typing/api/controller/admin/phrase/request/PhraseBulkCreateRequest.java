package dasi.typing.api.controller.admin.phrase.request;

import dasi.typing.api.service.admin.phrase.request.PhraseCreateServiceRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhraseBulkCreateRequest {

  private List<PhraseCreateRequest> phrases;

  public List<PhraseCreateServiceRequest> toServiceRequests() {
    return phrases.stream()
        .map(PhraseCreateRequest::toServiceRequest)
        .toList();
  }
}
