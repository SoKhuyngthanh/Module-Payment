package vn.DrinkOrder.Module_Payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.DrinkOrder.Module_Payment.entity.ThanhToan;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {
}
