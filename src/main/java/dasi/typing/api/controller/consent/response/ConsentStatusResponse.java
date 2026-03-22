package dasi.typing.api.controller.consent.response;

import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentType;
import lombok.Builder;

@Builder
public record ConsentStatusResponse(
    ConsentType type,
    String description,
    Integer currentVersion,
    Integer agreedVersion,
    boolean agreed
) {

  public static ConsentStatusResponse of(Consent activeConsent, Integer agreedVersion, boolean agreed) {
    return ConsentStatusResponse.builder()
        .type(activeConsent.getType())
        .description(activeConsent.getDescription())
        .currentVersion(activeConsent.getVersion())
        .agreedVersion(agreedVersion)
        .agreed(agreed)
        .build();
  }
}