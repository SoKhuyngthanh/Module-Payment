package vn.DrinkOrder.Module_Payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.DrinkOrder.Module_Payment.entity.DonHang;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {
}
