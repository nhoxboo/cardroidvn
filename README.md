# CarDroid (Phiên bản Tiếng Việt & Nâng cao)

<div align="center">

**Chạy ứng dụng Android, Android Auto và Samsung DeX trực tiếp trên màn hình Apple CarPlay không dây của ô tô.**

[![GitHub Stars](https://img.shields.io/github/stars/nhoxboo/CarDroid?style=flat-square)](https://github.com/nhoxboo/CarDroid/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/nhoxboo/CarDroid?style=flat-square)](https://github.com/nhoxboo/CarDroid/network)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green?style=flat-square)](https://github.com/nhoxboo/CarDroid)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue?style=flat-square)](LICENSE)

[Tải về APK mới nhất](https://github.com/nhoxboo/CarDroid/releases) · [Báo lỗi / Góp ý](https://github.com/nhoxboo/CarDroid/issues) · [Hướng dẫn đóng góp](#hướng-dẫn-đóng-góp-dự-án-contributing)

</div>

---

## Giới thiệu tổng quan

**CarDroid** biến chiếc điện thoại Android của bạn thành một thiết bị phát chuẩn **Apple CarPlay không dây**. Màn hình ô tô (Head Unit) chỉ cần hỗ trợ Apple CarPlay là có thể kết nối không dây, hiển thị đa nhiệm các ứng dụng Android, giao diện Android Auto chuẩn hoặc không gian máy tính **Samsung DeX** đa nhiệm, đồng thời nhận phản hồi cảm ứng từ màn hình xe và phím bấm trên vô lăng.

### Điểm nổi bật trong phiên bản này:
1. **Việt hóa 100%:** Toàn bộ giao diện điện thoại và màn hình xe đã được bản địa hóa tiếng Việt chuẩn mực.
2. **Cầu nối Android Auto qua Apple CarPlay:** Giả lập giao diện Android Auto Coolwalk (chia thẻ bản đồ + thẻ nghe nhạc Media + dock điều hướng) truyền mượt mà qua kênh CarPlay.
3. **Hỗ trợ đặc biệt cho Samsung Galaxy (Z Fold 6 / S Series):** Truyền không gian làm việc máy tính **Samsung DeX không dây** lên màn hình xe, điện thoại vẫn dùng bình thường.
4. **Tối ưu hóa giao thức (Protocol Optimization):** Rút ngắn khoảng cách Keyframe H.264 xuống **1 giây** (loại bỏ màn hình đen, khôi phục khung hình tức thì khi Wi-Fi chập chờn), hỗ trợ độ phân giải **1920x720 Ultrawide** chuẩn cho các dòng xe VinFast, Mazda, Hyundai, Kia...
5. **Hỗ trợ tối đa máy KHÔNG ROOT:** Tích hợp bộ công cụ kích hoạt 1-Click qua ADB (Windows/macOS), **không bắt buộc phải cài đặt hay chạy Shizuku**.

---

## So sánh các chế độ hiển thị

| Chế độ | Mô tả trải nghiệm trên xe | Phù hợp cho | Yêu cầu quyền |
| :--- | :--- | :--- | :--- |
| **1. Cầu nối Android Auto (Coolwalk UI)** | Giao diện chuẩn Android Auto: Thẻ bản đồ điều hướng lớn (65%) + Thẻ Media bắt nhạc từ Spotify/Zing MP3 (35%) + Dock ứng dụng. | Người thích giao diện chuẩn ô tô, dẫn đường và nghe nhạc mượt mà. | ADB 1 lần hoặc Shizuku |
| **2. Samsung DeX (Galaxy Z Fold / S Series)** | Biến màn hình xe thành máy tính DeX: Mở tự do nhiều cửa sổ app (Google Maps, Vietmap, YouTube, Web), màn hình điện thoại dùng độc lập. | Chủ xe dùng Samsung Galaxy Z Fold 6, Fold 5, S23, S24 Series... | Tự động kích hoạt khi có cờ DeX |
| **3. CarDroid Dual-Panel** | Chia đôi màn hình thành 2 cửa sổ độc lập chạy song song 2 ứng dụng bất kỳ (Ví dụ: YouTube + Bản đồ). | Người muốn xem video và xem bản đồ cùng lúc trên màn hình xe. | ADB 1 lần hoặc Shizuku |
| **4. Phản chiếu màn hình (Screen Mirror)** | Chiếu toàn bộ màn hình điện thoại lên xe với cảm ứng 2 chiều qua dịch vụ Trợ năng (Accessibility). | Dành cho máy thuần không root, không có máy tính để chạy ADB. | Trợ năng (Không cần ADB/Root) |

---

## Hướng dẫn cài đặt & Kích hoạt nhanh 1-Click (Không cần Root)

Không cần root máy và không cần cài đặt Shizuku! Bạn chỉ cần máy tính chạy script kích hoạt 1 lần duy nhất:

### Bước 1: Chuẩn bị trên điện thoại
1. Bật **Tùy chọn cho nhà phát triển (Developer Options)**: Vào *Cài đặt > Thông tin điện thoại > Thông tin phần mềm > Nhấn 7 lần vào "Số hiệu bản tạo"*.
2. Vào *Cài đặt > Tùy chọn cho nhà phát triển > Bật "Gỡ lỗi USB (USB Debugging)"*.
3. Cắm cáp USB kết nối điện thoại với máy tính, trên điện thoại chọn **"Luôn cho phép từ máy tính này"**.

### Bước 2: Chạy công cụ kích hoạt 1-Click
- **Trên Windows:** Tải mã nguồn về và nhấp đúp chạy file:
  ```text
  tools\cardroid-adb-setup.bat
  ```
- **Trên macOS / Linux:** Mở Terminal và chạy lệnh:
  ```bash
  bash tools/cardroid-adb-setup.sh
  ```

> **Cách kích hoạt trực tiếp từ điện thoại (Không cần máy tính - Android 11+):**  
> Mở CarDroid > Vào *Cài đặt & Tối ưu hóa* > Bấm nút **"📋 Sao chép lệnh ADB khởi chạy"** > Mở ứng dụng gỡ lỗi không dây nội bộ (LADB hoặc Shizuku nếu có) và dán lệnh để kích hoạt.

---

## Hướng dẫn kết nối với xe ô tô

```text
[Điện thoại Android]  ---(Bluetooth Handshake)--->  [Màn hình xe (CarPlay)]
[Điện thoại Android]  <---(Wi-Fi P2P / AP Handoff)---  [Màn hình xe (CarPlay)]
[Điện thoại Android]  ===(H.264 Video + PCM Audio)===>  [Màn hình xe (CarPlay)]
[Điện thoại Android]  <===(HID Touch / Steering Wheel)===  [Màn hình xe (CarPlay)]
```

1. **Khởi động:** Mở ứng dụng CarDroid trên điện thoại, đảm bảo Bluetooth đã bật.
2. **Bắt đầu:** Nhấn nút **"Bắt đầu"** trên màn hình chính của CarDroid để đưa điện thoại vào trạng thái sẵn sàng kết nối.
3. **Trên ô tô:** Bật màn hình xe và mở mục **Apple CarPlay**.
4. **Kết nối:** Chọn tên điện thoại của bạn hiển thị trên màn hình xe. Hai thiết bị sẽ tự động bắt tay qua Bluetooth và chuyển luồng truyền phát sang Wi-Fi tốc độ cao.

---

## Bảng ma trận độ phân giải khuyến nghị cho từng dòng xe

Vào mục **Cài đặt & Tối ưu hóa** trong CarDroid để thiết lập độ phân giải chuẩn cho xe:

| Dòng xe | Tỉ lệ màn hình | Độ phân giải khuyên dùng | Thiết lập FPS & Bitrate |
| :--- | :--- | :--- | :--- |
| **VinFast Lux A / SA, VF e34, VF8** | Màn hình dọc / Ultrawide | **Ultrawide (1920x720)** | 60 FPS, Bitrate 10–15 Mbps |
| **Mazda 3, CX-30, CX-5, CX-8** | Màn hình ngang dài | **Ultrawide (1920x720)** | 60 FPS, Bitrate 10 Mbps |
| **Hyundai Tucson, Santa Fe, Kia Sorento, Carnival** | Màn hình đôi ngang | **1080p FHD (1920x1080)** | 60 FPS, Bitrate 10–15 Mbps |
| **Màn hình Android gắn ngoài, xe phổ thông** | Màn hình 16:9 / 8:5 | **720p HD (1280x720)** hoặc **Tự động** | 30/60 FPS, Bitrate 6–10 Mbps |

---

## Hướng dẫn tự build APK từ mã nguồn

Dự án sử dụng Gradle 8.9 và Kotlin 2.0.20:

```bash
# 1. Clone repository về máy
git clone https://github.com/nhoxboo/CarDroid.git
cd CarDroid

# 2. Biên dịch và chạy Unit Tests
./gradlew :tools-runner:test

# 3. Build file APK Debug
./gradlew :app:assembleDebug

# 4. Cài đặt trực tiếp lên điện thoại qua ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Hướng dẫn đóng góp dự án (Contributing)

Chúng tôi luôn hoan nghênh mọi đóng góp từ cộng đồng (sửa lỗi, dịch thuật, tối ưu hóa giao thức, bổ sung tương thích với các dòng xe mới)!

### Quy trình gửi Pull Request (PR):
1. **Fork repository** về tài khoản GitHub của bạn.
2. **Tạo nhánh mới (Branch):**
   ```bash
   git checkout -b feature/ten-tinh-nang-moi
   ```
3. **Quy chuẩn mã nguồn:**
   - Tuân thủ phong cách Kotlin chuẩn (`kotlin.code.style=official`).
   - Mọi chuỗi text giao diện mới phải được khai báo trong `app/src/main/res/values/strings.xml` và `values-vi/strings.xml`.
   - Đảm bảo lệnh `./gradlew :tools-runner:test` chạy thành công không có lỗi.
4. **Commit & Push:**
   ```bash
   git commit -m "feat: mô tả ngắn gọn tính năng vừa bổ sung"
   git push origin feature/ten-tinh-nang-moi
   ```
5. **Mở Pull Request** trên GitHub kèm theo mô tả chi tiết và hình ảnh/video kiểm thử trên xe thật (nếu có).

---

## Giấy phép & Tuyên bố từ chối trách nhiệm (Disclaimer)

- **Giấy phép:** Dự án được phát hành theo giấy phép mã nguồn mở.
- **Tuyên bố:** *Apple CarPlay, iAP2 và MFi là thương hiệu đã đăng ký của Apple Inc. CarDroid là dự án nghiên cứu phát triển độc lập và không liên kết với Apple Inc hay Google LLC.*
- Dự án được phát triển và duy trì bởi [nhoxboo](https://github.com/nhoxboo) và cộng đồng nguồn mở từ [pavunato/CarDroid](https://github.com/pavunato/CarDroid).
