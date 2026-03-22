package dasi.typing.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.regex.Pattern;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class PatternUtil {

  public static final Pattern INVALID_CV_PATTERN = Pattern.compile(".*[ㄱ-ㅎㅏ-ㅣ].*");

  public static final Pattern ALLOWED_NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

}