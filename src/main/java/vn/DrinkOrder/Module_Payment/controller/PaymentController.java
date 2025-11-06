package vn.DrinkOrder.Module_Payment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.DrinkOrder.Module_Payment.dto.PaymentRequest;
import vn.DrinkOrder.Module_Payment.dto.PaymentResponse;

@RestController
@RequestMapping("/api/payment")
// Không cần @CrossOrigin nữa vì frontend và backend chung một nhà
public class PaymentController {

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        // 1. Nhận yêu cầu từ frontend
        long amount = request.getAmount();
        System.out.println("Nhan duoc yeu cau thanh toan so tien: " + amount);

        // --- Trong thực tế, đây là lúc bạn sẽ lưu đơn hàng vào CSDL với trạng thái "CHUA_THANH_TOAN" ---
        // long orderId = orderRepository.save(newOrder).getId();
        long orderId = System.currentTimeMillis(); // Tạm thời dùng timestamp làm mã đơn hàng

        // 2. Tạo chuỗi VietQR theo đúng chuẩn
        final String BANK_BIN = "970436"; // Vietcombank
        final String ACCOUNT_NO = "0123456789"; // Số tài khoản của bạn
        
        String paymentInfo = String.format("Thanh toan don hang %d", orderId);
        // Lưu ý: URL cho QR của VietQR thực tế là một chuỗi dài, không phải link ảnh
        // Nhưng để đơn giản, chúng ta dùng dịch vụ tạo ảnh của VietQR cho tiện demo
        String paymentUrl = String.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s", 
            BANK_BIN, ACCOUNT_NO, amount, paymentInfo.replace(" ", "%20"));

        System.out.println("Da tao link thanh toan VietQR: " + paymentUrl);

        // 3. Trả về link thanh toán cho frontend
        return new PaymentResponse("OK", "Tao thanh toan thanh cong!", paymentUrl);
    }
}