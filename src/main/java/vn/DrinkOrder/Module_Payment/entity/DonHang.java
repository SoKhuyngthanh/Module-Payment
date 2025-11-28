package vn.DrinkOrder.Module_Payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "don_hang")
public class DonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double tongTien;

    private String trangThaiDonHang; // "Chua thanh toan" | "Da thanh toan"

    private LocalDateTime ngayTao = LocalDateTime.now();
}
