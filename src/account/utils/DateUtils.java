package account.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

  private static final String PERIOD_FORMAT = "%02d-%04d";

  private static final String PERIOD_FORMAT_WITH_MONTH_TEXT = "%s-%04d";

  public static LocalDate periodToLocalDate(String period) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    return LocalDate.parse("01-"+period, formatter);
  }

  public static String localDateToPeriod(LocalDate localDate) {
    return String.format(PERIOD_FORMAT, localDate.getMonthValue(), localDate.getYear());
  }

  public static String getPeriodWithMonthTextAndYear(LocalDate localDate){
    String month = localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
    Integer year = localDate.getYear();
    return String.format(PERIOD_FORMAT_WITH_MONTH_TEXT, month, year);
  }
}
