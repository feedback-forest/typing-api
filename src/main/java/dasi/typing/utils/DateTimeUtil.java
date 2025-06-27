package dasi.typing.utils;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class DateTimeUtil {

  public static LocalDateTime getMonthStartDate(LocalDate now) {
    return now.withDayOfMonth(1).atTime(0, 0, 0);
  }

  public static LocalDateTime getMonthEndDate(LocalDate now) {
    return now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59, 999999999);
  }

  public static LocalDateTime getNextMonthStartDate(LocalDate now) {
    return now.plusMonths(1).withDayOfMonth(1).atTime(0, 0, 0);
  }
}
