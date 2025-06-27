package dasi.typing.jwt;

import static dasi.typing.exception.Code.EMPTY_JWT_TOKEN;
import static dasi.typing.exception.Code.EXPIRED_ACCESS_TOKEN;
import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INVALID_ACCESS_TOKEN;
import static dasi.typing.exception.Code.INVALID_REFRESH_TOKEN;
import static dasi.typing.exception.Code.UNSUPPORTED_JWT_TOKEN;
import static dasi.typing.utils.ConstantUtil.BEARER_PREFIX;
import static dasi.typing.utils.ConstantUtil.TOKEN_EXPIRE_TIME;
import static dasi.typing.utils.ConstantUtil.TOKEN_REFRESH_TIME;

import dasi.typing.domain.member.Role;
import dasi.typing.domain.refreshToken.RefreshToken;
import dasi.typing.domain.refreshToken.RefreshTokenRepository;
import dasi.typing.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final Key key;
  private final RefreshTokenRepository refreshTokenRepository;

  public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey,
      RefreshTokenRepository refreshTokenRepository) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public JwtToken generateToken(String kakaoId) {

    Date now = new Date();
    Date accessTokenExpiresIn = new Date(now.getTime() + TOKEN_EXPIRE_TIME);
    Date refreshTokenExpiresIn = new Date(now.getTime() + TOKEN_REFRESH_TIME);

    Claims claims = createClaims(kakaoId, now, accessTokenExpiresIn);

    String accessToken = Jwts.builder()
        .setClaims(claims)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    String refreshToken = Jwts.builder()
        .setSubject(UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setExpiration(refreshTokenExpiresIn)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    refreshTokenRepository.save(new RefreshToken(kakaoId, refreshToken));

    return new JwtToken(BEARER_PREFIX.trim(), accessToken, refreshToken);
  }

  public boolean validateAccessToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      throw new CustomException(INVALID_ACCESS_TOKEN);
    } catch (ExpiredJwtException e) {
      throw new CustomException(EXPIRED_ACCESS_TOKEN);
    } catch (UnsupportedJwtException e) {
      throw new CustomException(UNSUPPORTED_JWT_TOKEN);
    } catch (IllegalArgumentException e) {
      throw new CustomException(EMPTY_JWT_TOKEN);
    }
  }

  public boolean validateRefreshToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      throw new CustomException(INVALID_REFRESH_TOKEN);
    } catch (ExpiredJwtException e) {
      throw new CustomException(EXPIRED_REFRESH_TOKEN);
    } catch (UnsupportedJwtException e) {
      throw new CustomException(UNSUPPORTED_JWT_TOKEN);
    } catch (IllegalArgumentException e) {
      throw new CustomException(EMPTY_JWT_TOKEN);
    }
  }

  public Claims getClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  public String getKakaoId(final String token) {
    return getClaims(token).get("kakaoId", String.class);
  }

  private Claims createClaims(String kakaoId, Date now, Date accessTokenExpiresIn) {
    Claims claims = Jwts.claims()
        .setIssuer("typing")
        .setSubject("KAKAO_ID")
        .setIssuedAt(now)
        .setExpiration(accessTokenExpiresIn);

    claims.put("kakaoId", kakaoId);
    claims.put("role", Role.USER);
    return claims;
  }
}