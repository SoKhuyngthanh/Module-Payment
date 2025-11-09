package vn.DrinkOrder.Module_Payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class ModulePaymentApplication {
    public static void main(String[] args) {
        // Thiết lập múi giờ mặc định cho toàn bộ ứng dụng Spring Boot
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(ModulePaymentApplication.class, args);
    }
}
