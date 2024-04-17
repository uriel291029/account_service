package account.service;

import account.dto.request.PayrollRequest;
import account.exception.BadRequestException;
import account.model.Payroll;
import account.repository.PayrollRepository;
import account.repository.UserRepository;
import account.utils.DateUtils;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceHelper {

  private static final String DUPLICATED_PERIOD_ERROR_MESSAGE = "It's not possible to allocate the money more once during the same period";

  private static final Long ALLOWED_PERIOD_COUNT = 1L;

  private final PayrollRepository payrollRepository;

  private final UserRepository userRepository;

  public void validMoneyInASinglePeriod(List<PayrollRequest> payrollRequests) {
    Map<PayrollRequest, Long> periodCountMapFromRequest = payrollRequests.stream()
        .collect(Collectors.groupingBy(o -> PayrollRequest.builder()
            .employee(o.getEmployee())
            .period(o.getPeriod()).build(), Collectors.counting()));

    Set<String> employees = payrollRequests.stream()
        .map(PayrollRequest::getEmployee)
        .collect(Collectors.toSet());

    List<Payroll> payrolls = payrollRepository.findByEmployeeIn(employees);
    Map<PayrollRequest, Long> periodCountMapFromDataBase = payrolls.stream()
        .collect(Collectors.groupingBy(o ->
                PayrollRequest.builder()
                    .employee(o.getEmployee())
                    .period(DateUtils.localDateToPeriod(o.getPeriod())).build(),
            Collectors.counting()));

    Map<PayrollRequest, Long> periodCountMap = new HashMap<>(periodCountMapFromRequest);
    for (Map.Entry<PayrollRequest, Long> entry : periodCountMapFromDataBase.entrySet()) {
      if (periodCountMap.containsKey(entry.getKey())) {
        Long count = periodCountMap.get(entry.getKey());
        count = count + entry.getValue();
        periodCountMap.put(entry.getKey(), count);
      } else {
        periodCountMap.put(entry.getKey(), entry.getValue());
      }
    }

    Collection<Long> periodCounts = periodCountMap.values();
    periodCounts.removeIf(integer -> Objects.equals(integer, ALLOWED_PERIOD_COUNT));
    if (!periodCounts.isEmpty()) {
      log.error(DUPLICATED_PERIOD_ERROR_MESSAGE);
      throw new BadRequestException(DUPLICATED_PERIOD_ERROR_MESSAGE);
    }
  }

  public void validCurrentEmployees(List<PayrollRequest> payrollRequests){
    Set<String> employees = payrollRequests.stream()
        .map(PayrollRequest::getEmployee)
        .collect(Collectors.toSet());
    long employeesTotal = userRepository.countByEmailIn(employees);
    if(employeesTotal != employees.size()){
      log.error("There employees which are not in the database");
      throw new BadRequestException("There employees which are not in the database");
    }
  }
}
