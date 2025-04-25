package dasi.typing.api.controller.phrase;

import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.api.service.phrase.PhraseService;
import dasi.typing.exception.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'GUEST')")
public class PhraseController {

  private final PhraseService phraseService;

  @GetMapping("/api/v1/phrases")
  public ApiResponse<Map<String, List<PhraseResponse>>> getRandomPhrases() {
    List<PhraseResponse> responses = phraseService.getRandomPhrases();
    return ApiResponse.success("phrases", responses);
  }

}
