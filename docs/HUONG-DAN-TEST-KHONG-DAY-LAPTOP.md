# Hướng Dẫn Kiểm Thử Màn Hình Xe Không Dây (Wireless CarPlay Standard)

## 1. Cơ chế hoạt động chuẩn của Wireless Apple CarPlay
- **Xe hơi (Head Unit / Host Receiver):** Phát mạng Wi-Fi Access Point (AP Hotspot).
- **Điện thoại (Client / Transmitter - iPhone hoặc CarDroid):** Kết nối vào mạng Wi-Fi của Xe, sau đó stream luồng video H.264 (AirPlay RTSP) sang cổng của Xe.

---

## 2. Mô phỏng chuẩn trên Laptop & Galaxy Z Fold 6
- **Laptop đóng vai trò Xe hơi (Host):**
  - Bật tính năng **Mobile Hotspot** trên Windows (Laptop phát Wi-Fi).
  - Chạy chương trình mô phỏng màn hình ô tô (Receiver) lắng nghe kết nối.
- **Galaxy Z Fold 6 (Client):**
  - Bật Wi-Fi và kết nối vào mạng Hotspot do Laptop phát.
  - CarDroid truyền luồng màn hình xe không dây sang Laptop.

---

## 3. Các bước thiết lập trên Laptop (Windows)
1. **Bật Mobile Hotspot trên Laptop:**
   - Mở *Settings > Network & internet > Mobile hotspot*.
   - Bật **On**.
   - Đặt tên mạng Wi-Fi (SSID) và Mật khẩu (ví dụ: `CarPlay-Laptop`, pass: `12345678`).
2. **Trên Galaxy Z Fold 6:**
   - Vào Wi-Fi, chọn kết nối vào mạng `CarPlay-Laptop` vừa phát từ Laptop.
3. **Chạy Receiver trên Laptop:**
   - Nhấp đúp vào file `tools/test-wireless.bat` hoặc chạy:
     ```bash
     python tools/test-carplay-laptop.py --host auto
     ```
   - Cửa sổ màn hình xe ô tô sẽ mở ra trên Laptop, hiển thị mượt mà không dây từ Fold 6!
