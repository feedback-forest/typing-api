package dasi.typing.api.controller.admin.phrase;

import dasi.typing.api.controller.admin.phrase.request.PhraseBulkCreateRequest;
import dasi.typing.api.service.admin.phrase.AdminPhraseService;
import dasi.typing.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPhraseController {

  private final AdminPhraseService adminPhraseService;

  @PostMapping("/api/v1/phrases")
  public ApiResponse<Boolean> createPhrases(@RequestBody PhraseBulkCreateRequest request) {
    adminPhraseService.createPhrases(request.toServiceRequests());
    return ApiResponse.success(true);
  }
}
