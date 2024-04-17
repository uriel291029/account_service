package account.controller;

import account.dto.request.PayrollRequest;
import account.dto.response.PayrollGetResponse;
import account.dto.response.PayrollPostResponse;
import account.service.PaymentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class PaymentController {

  private final PaymentService paymentService;

  @RolesAllowed({"ROLE_ACCOUNTANT"})
  @PostMapping("acct/payments")
  public PayrollPostResponse uploadPayrolls(@RequestBody @Valid List<PayrollRequest> payrolls) {
    return paymentService.updatePayrolls(payrolls);
  }

  @RolesAllowed({"ROLE_ACCOUNTANT","ROLE_USER"})
  @GetMapping("empl/payment")
  public Object retrievePayrolls(@RequestParam Optional<String> period) {
    if (period.isPresent()) {
      return paymentService.retrievePayroll(period.get());
    }
    return paymentService.retrievePayrolls();
  }

  @RolesAllowed({"ROLE_ACCOUNTANT"})
  @PutMapping("acct/payments")
  public PayrollPostResponse uploadPayroll(@RequestBody @Valid PayrollRequest payroll) {
    return paymentService.updatePayroll(payroll);
  }
}
