package dasi.typing.api.controller.member;

import static dasi.typing.utils.ConstantUtil.BEARER_PREFIX;

import dasi.typing.api.controller.member.request.MemberCreateRequest;
import dasi.typing.api.controller.member.request.MemberNicknameRequest;
import dasi.typing.api.controller.member.response.NicknameResponse;
import dasi.typing.api.service.member.MemberService;
import dasi.typing.api.service.member.NicknameService;
import dasi.typing.exception.ApiResponse;
import dasi.typing.filter.GuestPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('GUEST')")
public class MemberController {

  private final MemberService memberService;
  private final NicknameService nicknameService;

  @GetMapping("/api/v1/members")
  public ResponseEntity<ApiResponse<Boolean>> signIn(
      @AuthenticationPrincipal GuestPrincipal guest) {

    String accessToken = memberService.signIn(guest.id());
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
        .body(ApiResponse.success(true));
  }

  @PostMapping("/api/v1/members")
  public ResponseEntity<ApiResponse<Boolean>> signUp(
      @AuthenticationPrincipal GuestPrincipal guest,
      @RequestBody MemberCreateRequest request) {

    String accessToken = memberService.signUp(guest.id(), request.toServiceRequest());
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
        .body(ApiResponse.success(true));
  }

  @PostMapping("/api/v1/members/nickname/validate")
  public ApiResponse<Boolean> validateNickname(@RequestBody MemberNicknameRequest request) {
    memberService.validateNickname(request.toServiceRequest());
    return ApiResponse.success(true);
  }

  @GetMapping("/api/v1/members/nickname/random")
  public ApiResponse<NicknameResponse> generateRandomNickname() {
    String nickname = nicknameService.generate();
    return ApiResponse.success(new NicknameResponse(nickname));
  }

  @PostMapping("/api/v1/members/reissue")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<Boolean>> reissue() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String kakaoId = (String) authentication.getPrincipal();
    String accessToken = memberService.reissue(kakaoId);

    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
        .body(ApiResponse.success(true));
  }
}
