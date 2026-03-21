package dasi.typing.api.controller.consent;

import dasi.typing.api.controller.consent.response.ConsentStatusResponse;
import dasi.typing.api.service.consent.ConsentService;
import dasi.typing.domain.consent.ConsentType;
import dasi.typing.exception.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ConsentController {

  private final ConsentService consentService;

  @GetMapping("/api/v1/consents/status")
  public ApiResponse<Map<String, List<ConsentStatusResponse>>> getConsentStatus() {
    String kakaoId = getKakaoId();
    List<ConsentStatusResponse> statuses = consentService.getConsentStatus(kakaoId);
    return ApiResponse.success("consents", statuses);
  }

  @PostMapping("/api/v1/consents/re-agree")
  public ApiResponse<Boolean> reAgree(@RequestBody List<ConsentType> agreements) {
    String kakaoId = getKakaoId();
    consentService.reAgree(kakaoId, agreements);
    return ApiResponse.success(true);
  }

  private String getKakaoId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (String) authentication.getPrincipal();
  }
}
