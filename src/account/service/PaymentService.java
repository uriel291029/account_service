package account.service;

import account.dto.request.PayrollRequest;
import account.dto.response.PayrollGetResponse;
import account.dto.response.PayrollPostResponse;
import account.dto.response.UserResponse;
import account.exception.BadRequestException;
import account.model.AuthUser;
import account.model.Payroll;
import account.repository.PayrollRepository;
import account.repository.UserRepository;
import account.utils.DateUtils;
import account.utils.PayrollTransformer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final UserRepository userRepository;

  private final PayrollRepository payrollRepository;

  private final PaymentServiceHelper paymentServiceHelper;

  public UserResponse retrievePayment() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.info("Retrieving the user with the email: {}", authentication.getName());
    Optional<AuthUser> optionalUser = userRepository.findByEmail(
        authentication.getName().toLowerCase());

    if (optionalUser.isPresent()) {
      AuthUser user = optionalUser.get();
      return UserResponse.builder()
          .id(user.getUserId())
          .name(user.getName())
          .lastname(user.getLastname())
          .email(user.getEmail())
          .build();
    }
    return null;
  }

  public PayrollPostResponse updatePayrolls(List<PayrollRequest> payrollRequests) {
    paymentServiceHelper.validCurrentEmployees(payrollRequests);
    paymentServiceHelper.validMoneyInASinglePeriod(payrollRequests);

    List<Payroll> payrolls;
    try {
      payrolls = payrollRequests.stream()
          .map(PayrollTransformer::payrollRequestToPayroll)
          .toList();
    } catch (DateTimeParseException exception) {
      log.error("It occurred an error in the conversion of the period to local date");
      throw new BadRequestException(
          "It occurred an error in the conversion of the period to local date");
    }
    payrollRepository.saveAll(payrolls);

    return PayrollPostResponse.builder().status("Added successfully!").build();
  }

  public PayrollPostResponse updatePayroll(PayrollRequest payrollRequest) {
    paymentServiceHelper.validCurrentEmployees(List.of(payrollRequest));
    LocalDate periodDate;
    try {
      periodDate = DateUtils.periodToLocalDate(payrollRequest.getPeriod());
    } catch (DateTimeParseException exception) {
      log.error("It occurred an error in the conversion of the period to local date");
      throw new BadRequestException(
          "It occurred an error in the conversion of the period to local date");
    }

    Optional<Payroll> payrollOptional = payrollRepository.findByEmployeeAndPeriod(
        payrollRequest.getEmployee(), periodDate);
    if (payrollOptional.isPresent()) {
      Payroll payroll = payrollOptional.get();
      payroll.setSalary(payrollRequest.getSalary());
      payrollRepository.save(payroll);
    } else {
      paymentServiceHelper.validMoneyInASinglePeriod(List.of(payrollRequest));
      Payroll payroll = PayrollTransformer.payrollRequestToPayroll(payrollRequest);
      payrollRepository.save(payroll);
    }
    return PayrollPostResponse.builder().status("Updated successfully!").build();
  }

  public List<PayrollGetResponse> retrievePayrolls() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.info("Retrieving the payrolls with the email: {}", authentication.getName());
    String email = authentication.getName();
    Optional<AuthUser> authUserOptional = userRepository.findByEmail(email);
    AuthUser authUser = authUserOptional.get();
    List<Payroll> payrolls = payrollRepository
        .findByEmployeeOrderByPeriodDesc(authentication.getName().toLowerCase());
    return payrolls.stream().map(payroll -> PayrollGetResponse.builder()
            .name(authUser.getName())
            .lastname(authUser.getLastname())
            .salary(PayrollTransformer.salaryToSalaryText(payroll.getSalary()))
            .period(DateUtils.getPeriodWithMonthTextAndYear(payroll.getPeriod())).build())
        .toList();
  }

  public PayrollGetResponse retrievePayroll(String period) {
    LocalDate periodDate;
    try {
      periodDate = DateUtils.periodToLocalDate(period);
    } catch (DateTimeParseException exception) {
      log.error("It occurred an error in the conversion of the period to local date");
      throw new BadRequestException(
          "It occurred an error in the conversion of the period to local date");
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.info("Retrieving the payrolls with the email: {}", authentication.getName());
    String email = authentication.getName();
    Optional<AuthUser> authUserOptional = userRepository.findByEmail(email);
    AuthUser authUser = authUserOptional.get();
    Optional<Payroll> payrollOptional = payrollRepository.findByEmployeeAndPeriod(
        authentication.getName().toLowerCase(), periodDate);
    if (payrollOptional.isPresent()) {
      Payroll payroll = payrollOptional.get();
      return PayrollGetResponse.builder()
          .name(authUser.getName())
          .lastname(authUser.getLastname())
          .salary(PayrollTransformer.salaryToSalaryText(payroll.getSalary()))
          .period(DateUtils.getPeriodWithMonthTextAndYear(payroll.getPeriod())).build();

    } else {
      return PayrollGetResponse.builder().build();
    }
  }
}
