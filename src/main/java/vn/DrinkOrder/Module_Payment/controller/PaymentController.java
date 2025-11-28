package vn.DrinkOrder.Module_Payment.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import vn.DrinkOrder.Module_Payment.dto.PaymentRequest;
import vn.DrinkOrder.Module_Payment.dto.PaymentResponse;
import vn.DrinkOrder.Module_Payment.entity.DonHang;
import vn.DrinkOrder.Module_Payment.entity.ThanhToan;
import vn.DrinkOrder.Module_Payment.repository.DonHangRepository;
import vn.DrinkOrder.Module_Payment.repository.ThanhToanRepository;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final DonHangRepository donHangRepo;
    private final ThanhToanRepository thanhToanRepo;

    // --- API 1: TẠO ĐƠN HÀNG ---
    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        // 1. Số tiền
        long finalAmount = 2000; 

        DonHang donHang = new DonHang();
        donHang.setTongTien((double) finalAmount);
        donHang.setTrangThaiDonHang("Chua thanh toan");
        donHangRepo.save(donHang);

        // 2. CẤU HÌNH TÀI KHOẢN ẢO (VA)
        // Ví dụ: Nếu SePay cấp cho bạn tài khoản MB Bank
        final String BANK_BIN = "970418"; // <--- Thay Mã BIN ngân hàng VA vào đây
        final String ACCOUNT_NO = "9624726112005"; // <--- Thay Số tài khoản VA vào đây (Copy từ SePay)

        // 3. Nội dung
        String paymentInfo = "DH" + donHang.getId(); 

        // 4. Tạo URL
        String paymentUrl = "https://img.vietqr.io/image/" + BANK_BIN + "-" + ACCOUNT_NO + "-qr_only.png"
                + "?amount=" + finalAmount
                + "&addInfo=" + paymentInfo;

        return new PaymentResponse("OK", "Tao don hang thanh cong", paymentUrl, donHang.getId());
    }

    // --- API 2: CHECK STATUS (Giữ nguyên cho Frontend hỏi) ---
    @GetMapping("/check-status")
    public ResponseEntity<?> checkPaymentStatus(@RequestParam Long donHangId) {
        DonHang donHang = donHangRepo.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don hang"));
        
        boolean isPaid = "Da thanh toan".equals(donHang.getTrangThaiDonHang());
        return ResponseEntity.ok(Map.of("donHangId", donHangId, "status", donHang.getTrangThaiDonHang(), "paid", isPaid));
    }

    // --- API 3: NHẬN WEBHOOK TỪ SEPAY (REAL TIME) ---
    // Bạn cần vào SePay -> Tích hợp Webhooks -> Điền link Ngrok vào
    @PostMapping("/sepay-callback")
    public ResponseEntity<?> handleSePayWebhook(
        @RequestHeader(value = "Authorization", required = false) String sePayApiKey, 
        @RequestBody JsonNode body
    ) {
        try {
            // Log dữ liệu SePay gửi về để kiểm tra
            System.out.println(">>> SEPAY DATA: " + body.toString());

            // SePay gửi thông tin giao dịch trực tiếp trong body
            // Các trường quan trọng: "content" (nội dung), "transferAmount" (số tiền)
            String description = body.get("content").asText(); 
            long amount = body.get("transferAmount").asLong();
            
            System.out.println(">>> GIAO DỊCH MỚI: " + description + " - " + amount + " VND");

            // Phân tích nội dung tìm ID đơn hàng (DH123...)
            Long donHangId = extractOrderId(description);

            if (donHangId != null) {
                DonHang donHang = donHangRepo.findById(donHangId).orElse(null);
                
                if (donHang != null && !"Da thanh toan".equals(donHang.getTrangThaiDonHang())) {
                    // Kiểm tra số tiền
                    if (amount >= donHang.getTongTien()) {
                        donHang.setTrangThaiDonHang("Da thanh toan");
                        donHangRepo.save(donHang);
                        
                        ThanhToan thanhToan = new ThanhToan();
                        thanhToan.setDonHang(donHang);
                        thanhToan.setPhuongThuc("SePay_BIDV");
                        thanhToan.setTrangThaiThanhToan("Da thanh toan");
                        thanhToan.setNgayThanhToan(LocalDateTime.now());
                        thanhToanRepo.save(thanhToan);
                        
                        System.out.println(">>> ĐÃ CẬP NHẬT ĐƠN HÀNG " + donHangId + " THÀNH CÔNG!");
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error");
        }
    }

    // Hàm tách ID đơn hàng
    private Long extractOrderId(String content) {
        try {
            Pattern pattern = Pattern.compile("DH(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (Exception e) { return null; }
        return null;
    }
}