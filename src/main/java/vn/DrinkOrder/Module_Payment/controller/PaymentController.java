package vn.DrinkOrder.Module_Payment.controller;

import org.springframework.web.bind.annotation.*;
import vn.DrinkOrder.Module_Payment.dto.PaymentRequest;
import vn.DrinkOrder.Module_Payment.dto.PaymentResponse;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        System.out.println("Nhan duoc yeu cau thanh toan so tien: " + request.getAmount());

        String fakePaymentUrl = "https://momo.vn/pay?orderId=12345&amount=" + request.getAmount();

        return new PaymentResponse("OK", "Tao thanh toan thanh cong!", fakePaymentUrl);
    }
}