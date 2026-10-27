# Kế hoạch và Kiến trúc Phát triển CarDroid (Vietnamese & Enhanced Protocol)

## 1. Mục tiêu dự án
1. **Việt hoá toàn diện (100% Vietnamese Localization):**
   - Đầy đủ tài nguyên `values-vi/strings.xml` và `values/strings.xml`.
   - Chuyển toàn bộ chuỗi text hardcoded trên Jetpack Compose UI (Launcher xe, Cài đặt, Thông báo, Hướng dẫn kết nối) sang chuỗi bản địa hóa.
2. **Tối ưu Protocol (CarPlay Protocol & Media Stream Optimization):**
   - Tối ưu H.264 Encoder (Adaptive bitrate, FPS, Keyframe interval, hỗ trợ độ phân giải linh hoạt 800x480, 1280x720, 1920x720 Ultrawide VinFast/Mazda/Hyundai, 1920x1080).
   - Tối ưu TCP/UDP buffer, AirPlay RTSP connection timeout & keep-alive, tự động phục hồi kết nối.
   - Tối ưu luồng âm thanh PCM/AAC, chống lag, chống rè tiếng.
3. **Chức năng chuyển giao thức Android Auto qua Apple CarPlay (AA-to-CarPlay Bridge):**
   - Thêm chế độ hiển thị phong cách **Android Auto Coolwalk UI**:
     - Taskbar bên hông hoặc bên dưới tích hợp nút App Drawer, hiển thị đồng hồ, pin, sóng.
     - Widget bản đồ lớn bên trái / trên.
     - Widget giải trí Media đa phương tiện (tích hợp Android MediaSession: tự động bắt bài hát, ảnh album bìa, nút Next/Prev/Pause từ Spotify, Zing MP3, YouTube Music...).
     - Lựa chọn 3 chế độ hiển thị: Dual-Panel (gốc), Android Auto Coolwalk (mới), và Fullscreen Mirror.
4. **Hỗ trợ tối đa cho máy không root & hạn chế dùng Shizuku:**
   - **Chế độ Zero-Setup (Không Root, Không Shizuku, Không ADB):** Sử dụng MediaProjection API + Accessibility Service (RemoteControlService) để stream và nhận cảm ứng trực tiếp từ xe.
   - **Chế độ Cấp quyền 1 lần qua ADB (No-Shizuku One-Time ADB):** Tích hợp script kích hoạt nhanh từ PC và hướng dẫn chi tiết ngay trên app; App tự động phát hiện port helper 8253 nếu đã chạy qua ADB shell.
   - **Cơ chế Fallback thông minh:** Tự động phát hiện môi trường để chọn phương thức tốt nhất mà không làm phiền người dùng.

---
## 2. Các giai đoạn triển khai
- **Giai đoạn 1:** Xây dựng hệ thống tài nguyên chuỗi đa ngôn ngữ (En / Vi) và Việt hóa giao diện.
- **Giai đoạn 2:** Bổ sung cấu hình tối ưu Protocol (Encoder bitrate, FPS, Resolution, Buffer, Reconnect logic) vào `CarDroidSettings` và pipeline.
- **Giai đoạn 3:** Xây dựng module Android Auto Coolwalk UI & MediaSession Controller chuyển đổi sang luồng CarPlay.
- **Giai đoạn 4:** Nâng cấp chế độ Non-Root (MediaProjection fallback mượt mà) + Script ADB tự động hóa 1-click.
- **Giai đoạn 5:** Kiểm thử, verify và commit đẩy lên GitHub `nhoxboo/CarDroid`.
