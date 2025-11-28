package vn.DrinkOrder.Module_Payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_don_hang")
    private DonHang donHang;

    private String phuongThuc; // vietqr, visa
    private String trangThaiThanhToan; // Chua thanh toan | Da thanh toan
    private LocalDateTime ngayThanhToan;
}
