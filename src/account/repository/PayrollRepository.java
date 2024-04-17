package account.repository;

import account.model.Payroll;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

  List<Payroll> findByEmployeeOrderByPeriodDesc(String employee);

  Optional<Payroll> findByEmployeeAndPeriod(String employee, LocalDate period);
  List<Payroll> findByEmployeeIn(Set<String> employees);

}
