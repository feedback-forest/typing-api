package dasi.typing.api.controller.mypage;

import dasi.typing.api.controller.mypage.response.MyPageResponse;
import dasi.typing.api.service.mypage.MyPageService;
import dasi.typing.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class MyPageController {

  private final MyPageService myPageService;

  @GetMapping("/api/v1/mypage")
  public ApiResponse<MyPageResponse> getMyPage() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String kakaoId = (String) authentication.getPrincipal();

    MyPageResponse response = myPageService.getMyPage(kakaoId);
    return ApiResponse.success(response);
  }
}
