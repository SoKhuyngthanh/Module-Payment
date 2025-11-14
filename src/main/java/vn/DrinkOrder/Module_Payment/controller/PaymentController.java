package vn.DrinkOrder.Module_Payment.controller;

import org.springframework.web.bind.annotation.*;
import vn.DrinkOrder.Module_Payment.dto.PaymentRequest;
import vn.DrinkOrder.Module_Payment.dto.PaymentResponse;
import vn.DrinkOrder.Module_Payment.entity.DonHang;
import vn.DrinkOrder.Module_Payment.entity.ThanhToan;
import vn.DrinkOrder.Module_Payment.repository.DonHangRepository;
import vn.DrinkOrder.Module_Payment.repository.ThanhToanRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final DonHangRepository donHangRepo;
    private final ThanhToanRepository thanhToanRepo;

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        // 1. Tạo đơn hàng
        DonHang donHang = new DonHang();
        donHang.setTongTien((double) request.getAmount());
        donHang.setTrangThaiDonHang("Chua thanh toan");
        donHangRepo.save(donHang);

        // 2. Tạo VietQR link (demo)
        final String BANK_BIN = "970436";
        final String ACCOUNT_NO = "0123456789";
        String paymentInfo = "Thanh toan don hang " + donHang.getId();
        String paymentUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s",
                BANK_BIN, ACCOUNT_NO, request.getAmount(), paymentInfo.replace(" ", "%20")
        );

        return new PaymentResponse("OK", "Tao thanh toan thanh cong!", paymentUrl, donHang.getId());
    }

    @PostMapping("/confirm")
    public PaymentResponse confirmPayment(@RequestParam Long donHangId, @RequestParam String phuongThuc) {
        DonHang donHang = donHangRepo.findById(donHangId).orElseThrow();
        donHang.setTrangThaiDonHang("Da thanh toan");
        donHangRepo.save(donHang);

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setDonHang(donHang);
        thanhToan.setPhuongThuc(phuongThuc);
        thanhToan.setTrangThaiThanhToan("Da thanh toan");
        thanhToan.setNgayThanhToan(LocalDateTime.now());
        thanhToanRepo.save(thanhToan);

        return new PaymentResponse("OK", "Thanh toan thanh cong!", null, donHangId);
    }
}
