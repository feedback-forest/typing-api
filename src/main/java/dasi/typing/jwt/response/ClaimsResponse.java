package dasi.typing.jwt.response;

import io.jsonwebtoken.Claims;

public record ClaimsResponse(Claims claims, boolean expired) {

}