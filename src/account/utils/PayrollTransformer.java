package account.utils;

import account.dto.request.PayrollRequest;
import account.model.Payroll;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayrollTransformer {

  private static final String SALARY_FORMAT = "%s dollar(s) %s cent(s)";

  public static Payroll payrollRequestToPayroll(PayrollRequest payrollRequest) {
    Payroll.PayrollBuilder payrollBuilder = Payroll.builder();
    payrollBuilder.employee(payrollRequest.getEmployee())
        .salary(payrollRequest.getSalary())
        .period(DateUtils.periodToLocalDate(payrollRequest.getPeriod()));
    return payrollBuilder.build();
  }

  public static String salaryToSalaryText(Long salary) {
    float salaryInDollars = salary / 100f;
    String salaryLegend = Float.toString(salaryInDollars);
    String dollars = salaryLegend.split("\\.")[0];
    String cents = salaryLegend.split("\\.")[1];
    return String.format(SALARY_FORMAT, dollars, cents);
  }
}
