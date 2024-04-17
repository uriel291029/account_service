package account.repository;

import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Repository;

@Getter
@Repository
public class PasswordRepository {

  private final List<String> breachedPassWords = List.of("PasswordForJanuary", "PasswordForFebruary",
      "PasswordForMarch", "PasswordForApril",
      "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
      "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

}
