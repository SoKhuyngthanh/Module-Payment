package vn.DrinkOrder.Module_Payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String status;
    private String message;
    private String paymentUrl;
}