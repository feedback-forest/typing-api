package dasi.typing.api.controller;

import dasi.typing.api.controller.request.TypingCreateRequest;
import dasi.typing.api.service.TypingService;
import dasi.typing.api.service.response.TypingResponse;
import dasi.typing.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TypingController {

  private final TypingService typingService;

  @PostMapping("/api/v1/typings")
  public ApiResponse<?> createTyping(@RequestBody TypingCreateRequest request) {

    TypingResponse response = typingService.createTyping(request.toServiceRequest());

    return ApiResponse.success(response);
  }

}
