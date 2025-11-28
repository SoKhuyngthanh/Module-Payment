const axios = require('axios');

// CẤU HÌNH: Đảm bảo Spring Boot đang chạy ở cổng 8080
const BASE_URL = 'http://localhost:8081/api/payment';

describe('Kịch bản kiểm thử luồng Thanh toán VietQR & SePay Webhook', () => {
    
    let donHangId; // Biến lưu ID đơn hàng để dùng chung cho các bước
    const amountToPay = 2000; // Số tiền test (khớp với logic tối thiểu trong code Java)

    // BƯỚC 1: KHÁCH HÀNG TẠO ĐƠN HÀNG
    test('Scenario 1: Tạo đơn hàng mới thành công (API /create)', async () => {
        const payload = { amount: amountToPay };
        
        try {
            const response = await axios.post(`${BASE_URL}/create`, payload);
            
            // Kỳ vọng: Server trả về 200 OK
            expect(response.status).toBe(200);
            
            // Kỳ vọng: Có trả về ID đơn hàng và Link QR
            expect(response.data.donHangId).toBeDefined();
            expect(response.data.paymentUrl).toContain("https://img.vietqr.io");
            
            // Lưu lại ID để dùng cho bước sau
            donHangId = response.data.donHangId;
            console.log(`[BƯỚC 1] ✅ Tạo thành công đơn hàng ID: ${donHangId} - Số tiền: ${amountToPay}đ`);
        } catch (error) {
            console.error("Lỗi Bước 1:", error.message);
            throw error;
        }
    });

    // BƯỚC 2: KIỂM TRA TRẠNG THÁI BAN ĐẦU
    test('Scenario 2: Trạng thái ban đầu phải là "Chua thanh toan"', async () => {
        const response = await axios.get(`${BASE_URL}/check-status`, {
            params: { donHangId: donHangId }
        });

        expect(response.status).toBe(200);
        expect(response.data.status).toBe("Chua thanh toan");
        expect(response.data.paid).toBe(false);
        console.log(`[BƯỚC 2] ✅ Đơn hàng ${donHangId} đang ở trạng thái chờ thanh toán.`);
    });

    // BƯỚC 3: GIẢ LẬP SEPAY BẮN WEBHOOK (QUAN TRỌNG NHẤT)
    // Bước này chứng minh "Thủ tục chạy" khi có tiền về
    test('Scenario 3: Webhook SePay báo có tiền về (API /sepay-callback)', async () => {
        // Giả lập gói tin JSON mà SePay gửi tới
        const sePayPayload = {
            "gateway": "MBBank",
            "transactionDate": "2025-11-13 10:00:00",
            "accountNumber": "SEPAY123456", // Số nào cũng được vì test logic
            "content": `THANH TOAN DH${donHangId}`, // QUAN TRỌNG: Phải chứa ID đơn hàng vừa tạo
            "transferType": "in",
            "transferAmount": amountToPay, // Số tiền phải khớp hoặc lớn hơn
            "id": 99999
        };

        console.log(`[BƯỚC 3] 🔄 Đang giả lập SePay bắn tin: "Tiền đã về cho đơn ${donHangId}"...`);

        const response = await axios.post(`${BASE_URL}/sepay-callback`, sePayPayload);

        expect(response.status).toBe(200);
        expect(response.data.success).toBe(true);
        console.log(`[BƯỚC 3] ✅ Server đã nhận tín hiệu Webhook thành công.`);
    });

    // BƯỚC 4: KIỂM TRA LẠI KẾT QUẢ CUỐI CÙNG
    test('Scenario 4: Đơn hàng tự động chuyển sang "Da thanh toan"', async () => {
        // Gọi lại API kiểm tra trạng thái
        const response = await axios.get(`${BASE_URL}/check-status`, {
            params: { donHangId: donHangId }
        });

        expect(response.status).toBe(200);
        
        // KỲ VỌNG QUAN TRỌNG NHẤT: Trạng thái phải đổi
        expect(response.data.status).toBe("Da thanh toan");
        expect(response.data.paid).toBe(true);
        
        console.log(`[BƯỚC 4] ✅ KIỂM TRA THÀNH CÔNG! Đơn hàng ${donHangId} đã hoàn tất.`);
    });
});