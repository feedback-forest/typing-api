package dasi.typing.api.controller.typing;

import dasi.typing.api.controller.typing.request.TypingCreateRequest;
import dasi.typing.api.service.typing.TypingService;
import dasi.typing.api.service.typing.response.TypingResponse;
import dasi.typing.exception.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER, GUEST')")
public class TypingController {

  private final TypingService typingService;

  @PostMapping("/api/v1/typings")
  public ApiResponse<Map<String, TypingResponse>> saveTyping(
      @RequestBody TypingCreateRequest request) {
    TypingResponse response = typingService.saveTyping(request.toServiceRequest());
    return ApiResponse.success("typing", response);
  }
}
