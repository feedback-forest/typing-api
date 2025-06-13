package dasi.typing.utils;

import static dasi.typing.utils.DateTimeUtil.getMonthStartDate;
import static dasi.typing.utils.DateTimeUtil.getNextMonthStartDate;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateTimeUtilTest {

  @Test
  @DisplayName("0시 0분 0초와 0시 0분 0초 0나노초가 동일한지 확인한다.")
  void zeroTimeEqualsZeroTimeZeroNanosTest() throws Exception {
    // given
    LocalDateTime monthStartDate = getMonthStartDate(now());
    LocalDateTime comparedTime = now().withDayOfMonth(1).atTime(0, 0, 0, 0);

    // when & then
    assertThat(monthStartDate).isEqualTo(comparedTime);
  }

  @Test
  @DisplayName("0시 0분 0초와 0시 0분 0초 000000001 나노초가 동일하지 않은지 확인한다.")
  void zeroTimeIsNotEqualToZeroTimeWithTinyNanoOffsetTest() throws Exception {
    // given
    LocalDateTime monthStartDate = getMonthStartDate(now());
    LocalDateTime comparedTime = now().withDayOfMonth(1).atTime(0, 0, 0, 000_000_001);

    System.out.println("monthStartDate = " + monthStartDate);
    System.out.println("comparedTime = " + comparedTime);

    // when & then
    assertThat(monthStartDate).isNotEqualTo(comparedTime).isBefore(comparedTime);
  }

  private static Stream<Arguments> provideTestDatesForNextMonthCalculation() {
    return Stream.of(
        Arguments.of(LocalDate.of(2023, 1, 31)),
        Arguments.of(LocalDate.of(2023, 2, 28)),
        Arguments.of(LocalDate.of(2023, 3, 31)),
        Arguments.of(LocalDate.of(2023, 4, 1)),
        Arguments.of(LocalDate.of(2023, 5, 1)),
        Arguments.of(LocalDate.of(2023, 6, 3)),
        Arguments.of(LocalDate.of(2023, 7, 5)),
        Arguments.of(LocalDate.of(2023, 8, 6)),
        Arguments.of(LocalDate.of(2023, 9, 14)),
        Arguments.of(LocalDate.of(2023, 10, 20)),
        Arguments.of(LocalDate.of(2023, 11, 30)),
        Arguments.of(LocalDate.of(2023, 12, 31))
    );
  }

  @MethodSource("provideTestDatesForNextMonthCalculation")
  @DisplayName("다음 달 1일 0시 0분 0초가 올바르게 계산되는지 확인한다. 만약 현재가 12월이라면 다음 해 1월 1일이 되어야 한다.")
  @ParameterizedTest(name = "[{index}] - 테스트 날짜: {0}")
  void plusMonthsOneAndFirstDayOfMonthAtStartOfDayCalculationTest(LocalDate testLocalDate)
      throws Exception {
    // given
    LocalDate now = testLocalDate;

    // when
    LocalDateTime nextMonthLocalDate = getNextMonthStartDate(now);
    LocalDateTime expected = calculateNextMonthStartDate(now);

    // then
    assertThat(nextMonthLocalDate).isEqualTo(expected);
  }


  private LocalDateTime calculateNextMonthStartDate(LocalDate now) {

    int nowYear = now.getYear();
    int nextMonth = now.getMonthValue() + 1;

    if (nextMonth > 12) {
      nextMonth = 1;
      nowYear++;
    }

    return LocalDateTime.of(nowYear, nextMonth, 1, 0, 0, 0);
  }
}