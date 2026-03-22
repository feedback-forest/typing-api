package dasi.typing.jwt;

public record JwtToken(
    String grantType,
    String accessToken,
    String refreshToken
) {

}
