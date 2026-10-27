#!/usr/bin/env python3
"""
CarPlay & Display Helper Simulator for Laptop (Wireless & USB)
Author: Hoài Nam (cardroidvn)
License: GPL v3

Allows testing cardroidvn directly on your PC/Laptop without an actual car:
- Wireless over Wi-Fi (Direct IP connection to port 8253)
- Wireless ADB / USB ADB forward
- Live H.264 video rendering with low-latency ffplay
"""

import sys
import os
import socket
import struct
import subprocess
import threading
import time

HELPER_PORT = 8253
PROTO_VERSION = 6

ROLE_CONTROL = 0
ROLE_VIDEO = 1

T_HELLO = 1
T_HELLO_ACK = 2
T_START_DISPLAY = 6
T_VIDEO_CONFIG = 12
T_VIDEO_FRAME = 13

def read_exact(sock, length):
    buf = bytearray()
    while len(buf) < length:
        chunk = sock.recv(length - len(buf))
        if not chunk:
            raise EOFError("Socket closed prematurely")
        buf.extend(chunk)
    return bytes(buf)

def recv_frame(sock):
    header = read_exact(sock, 5)
    length, mtype = struct.unpack(">IB", header)
    payload = read_exact(sock, length)
    return mtype, payload

def send_frame(sock, mtype, payload=b""):
    length = len(payload)
    sock.sendall(struct.pack(">IB", length, mtype) + payload)

def detect_device_ip(adb_cmd):
    try:
        res = subprocess.run([adb_cmd, "devices"], capture_output=True, text=True, check=True)
        for line in res.stdout.splitlines():
            parts = line.strip().split()
            if len(parts) >= 2 and parts[1] == "device":
                dev = parts[0]
                if ":" in dev:
                    return dev.split(":")[0]
    except Exception:
        pass
    return "192.168.1.108"

def run_laptop_test(host=None, width=1280, height=720, dpi=160, fps=60, bitrate=8000000):
    print("===================================================================")
    print("   CARDROIDVN - MÔ PHỎNG MÀN HÌNH XE CARPLAY (KHÔNG DÂY / WI-FI)  ")
    print("                 Bản quyền © Hoài Nam                              ")
    print("===================================================================")
    print()

    adb_cmd = "adb"
    for candidate in [
        r"C:\Android\sdk\platform-tools\adb.exe",
        os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"),
        os.path.expandvars(r"%USERPROFILE%\.openclaw\workspace\platform-tools\adb.exe")
    ]:
        if os.path.exists(candidate):
            adb_cmd = candidate
            break

    target_host = host
    if not target_host or target_host == "auto":
        target_host = detect_device_ip(adb_cmd)
        if not target_host:
            target_host = "127.0.0.1"

    print(f"[*] Chế độ kết nối: {'KHÔNG DÂY WI-FI' if target_host != '127.0.0.1' else 'LOCALHOST (ADB)'}")
    print(f"[*] Địa chỉ mục tiêu: {target_host}:{HELPER_PORT}")
    print()

    control_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    control_sock.settimeout(4.0)

    try:
        print(f"[1/4] Đang thử kết nối trực tiếp tới {target_host}:{HELPER_PORT}...")
        control_sock.connect((target_host, HELPER_PORT))
        print("  -> Kết nối trực tiếp qua mạng Wi-Fi thành công!")
    except Exception as direct_err:
        print(f"  [Thông báo] Kết nối trực tiếp {target_host} chưa mở: {direct_err}")
        print("  -> Đang chuyển tiếp qua cổng ADB (Wi-Fi ADB / USB)...")
        try:
            subprocess.run([adb_cmd, "forward", f"tcp:{HELPER_PORT}", f"tcp:{HELPER_PORT}"], check=True)
            target_host = "127.0.0.1"
            control_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            control_sock.connect((target_host, HELPER_PORT))
            print("  -> Đã kết nối thành công qua cổng chuyển tiếp!")
        except Exception as e:
            print(f"[LỖI] Không thể kết nối tới Display Helper: {e}")
            print("Vui lòng đảm bảo bạn đã chạy file 'tools\\cardroid-adb-setup.bat' trên máy tính!")
            return

    control_sock.settimeout(None)

    hello_payload = struct.pack(">BBIi", PROTO_VERSION, ROLE_CONTROL, 3, 10000)
    send_frame(control_sock, T_HELLO, hello_payload)
    
    ack_type, ack_payload = recv_frame(control_sock)
    if ack_type != T_HELLO_ACK:
        print(f"[LỖI] Helper từ chối kết nối (mtype={ack_type})")
        return
    print("  -> Xác thực kênh điều khiển thành công!")

    print(f"[2/4] Yêu cầu khởi tạo màn hình ô tô ({width}x{height} @ {fps}fps)...")
    start_payload = struct.pack(">HHHBI", width, height, dpi, fps, bitrate)
    send_frame(control_sock, T_START_DISPLAY, start_payload)

    video_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    time.sleep(0.3)
    video_sock.connect((target_host, HELPER_PORT))
    video_hello = struct.pack(">BBIi", PROTO_VERSION, ROLE_VIDEO, 3, 10000)
    send_frame(video_sock, T_HELLO, video_hello)
    
    ack_type, ack_payload = recv_frame(video_sock)
    if ack_type != T_HELLO_ACK:
        print(f"[LỖI] Kênh video bị từ chối")
        return
    print("  -> Kết nối kênh Video H.264 không dây thành công!")

    print("[3/4] Đang mở cửa sổ hiển thị màn hình ô tô CarPlay (ffplay)...")
    ffplay_cmd = [
        "ffplay",
        "-loglevel", "quiet",
        "-flags", "low_delay",
        "-framedrop",
        "-fflags", "nobuffer",
        "-window_title", f"CarDroidVN - Màn hình xe CarPlay ({width}x{height}) [Wi-Fi Stream]",
        "-x", str(min(width, 1280)),
        "-y", str(min(height, 720)),
        "-i", "-"
    ]

    try:
        player = subprocess.Popen(ffplay_cmd, stdin=subprocess.PIPE)
    except Exception as e:
        print(f"[LỖI] Không thể mở ffplay: {e}")
        return

    print("===================================================================")
    print(" [✓] ĐANG PHÁT LUỒNG MÀN HÌNH XE Ô TÔ TRỰC TIẾP LÊN LAPTOP!")
    print(" Nhấn phím 'Q' hoặc đóng cửa sổ video để dừng phiên test.")
    print("===================================================================")

    ANNEX_B_START = b"\x00\x00\x00\x01"
    try:
        while True:
            mtype, payload = recv_frame(video_sock)
            if mtype == T_VIDEO_CONFIG:
                sps_len, = struct.unpack_from(">H", payload, 0)
                sps = payload[2:2 + sps_len]
                pps_len, = struct.unpack_from(">H", payload, 2 + sps_len)
                pps = payload[4 + sps_len:4 + sps_len + pps_len]
                player.stdin.write(ANNEX_B_START + sps + ANNEX_B_START + pps)
                player.stdin.flush()
            elif mtype == T_VIDEO_FRAME:
                data = payload[9:]
                player.stdin.write(data)
                player.stdin.flush()
    except (KeyboardInterrupt, BrokenPipeError, EOFError):
        print("\n[THÔNG BÁO] Đã đóng phiên mô phỏng màn hình.")
    finally:
        try:
            player.stdin.close()
            player.terminate()
        except:
            pass
        control_sock.close()
        video_sock.close()

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="CarDroidVN Laptop Wireless Simulator")
    parser.add_argument("--host", default="auto", help="IP address of Android phone (or 'auto')")
    parser.add_argument("--ultrawide", action="store_true", help="1920x720 Ultrawide screen")
    args = parser.parse_args()

    w = 1920 if args.ultrawide else 1280
    h = 720
    run_laptop_test(host=args.host, width=w, height=h)
