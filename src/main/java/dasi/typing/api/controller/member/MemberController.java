package dasi.typing.api.controller.member;

import dasi.typing.api.controller.member.request.MemberCreateRequest;
import dasi.typing.api.controller.member.request.MemberNicknameRequest;
import dasi.typing.api.controller.member.response.NicknameResponse;
import dasi.typing.api.service.member.MemberService;
import dasi.typing.api.service.member.NicknameService;
import dasi.typing.exception.ApiResponse;
import dasi.typing.jwt.GuestPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('GUEST')")
public class MemberController {

  private final MemberService memberService;
  private final NicknameService nicknameService;

  @GetMapping("/api/v1/members")
  public ResponseEntity<ApiResponse<Boolean>> signIn(
      @AuthenticationPrincipal GuestPrincipal guest) {
    String accessToken = memberService.signIn(guest.getId());
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .body(ApiResponse.success(true));
  }

  @PostMapping("/api/v1/members")
  public ResponseEntity<ApiResponse<Boolean>> signUp(
      @AuthenticationPrincipal GuestPrincipal guest,
      @RequestBody MemberCreateRequest request) {
    String accessToken = memberService.signUp(guest.getId(), request.toServiceRequest());
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .body(ApiResponse.success(true));
  }

  @PostMapping("/api/v1/members/nickname")
  public ApiResponse<Boolean> validateNickname(@RequestBody MemberNicknameRequest request) {
    memberService.validateNickname(request.toServiceRequest());
    return ApiResponse.success(true);
  }

  @GetMapping("/api/v1/members/nickname/random")
  public ApiResponse<NicknameResponse> generateRandomNickname() {
    String nickname = nicknameService.generate();
    return ApiResponse.success(NicknameResponse.builder()
        .nickname(nickname).build());
  }
}
