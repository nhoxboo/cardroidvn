#!/usr/bin/env python3
"""
CarPlay & Display Helper Simulator for Laptop (Windows/macOS/Linux)
Author: Hoài Nam (cardroidvn)
License: GPL v3

Allows testing cardroidvn directly on your PC/Laptop without an actual car:
1. Connects to the cardroid-helper daemon on phone (via ADB port 8253)
2. Requests a simulated Car Display (1280x720 or 1920x720)
3. Streams H.264 video frames and renders them live via ffplay
"""

import sys
import os
import socket
import struct
import subprocess
import threading
import time

HELPER_PORT = 8253
MAGIC = b"CDRD"
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

def run_laptop_test(width=1280, height=720, dpi=160, fps=60, bitrate=8000000):
    print("===================================================================")
    print("     CARDROIDVN - MÔ PHỎNG MÀN HÌNH XE CARPLAY TRÊN LAPTOP        ")
    print("                 Bản quyền © Hoài Nam                              ")
    print("===================================================================")
    print()

    # 1. Forward adb port
    print("[1/4] Đang chuyển tiếp cổng ADB 8253 từ điện thoại...")
    adb_cmd = "adb"
    for candidate in [
        r"C:\Android\sdk\platform-tools\adb.exe",
        os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"),
        os.path.expandvars(r"%USERPROFILE%\.openclaw\workspace\platform-tools\adb.exe")
    ]:
        if os.path.exists(candidate):
            adb_cmd = candidate
            break

    try:
        subprocess.run([adb_cmd, "forward", f"tcp:{HELPER_PORT}", f"tcp:{HELPER_PORT}"], check=True)
        print("  -> Đã mở cổng ADB forward thành công.")
    except Exception as e:
        print(f"  [CẢNH BÁO] Không thể chạy adb forward: {e}")
        print("  Vui lòng đảm bảo điện thoại đã kết nối USB Debugging.")

    # 2. Connect Control Channel
    print(f"[2/4] Đang kết nối tới cardroid-helper tại 127.0.0.1:{HELPER_PORT}...")
    control_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        control_sock.connect(("127.0.0.1", HELPER_PORT))
    except Exception as e:
        print(f"[LỖI] Không thể kết nối tới Display Helper: {e}")
        print("Hãy chạy file 'tools\\cardroid-adb-setup.bat' trước để khởi động Helper!")
        return

    # Send HELLO for CONTROL role
    # HELLO payload: u8 protoVersion, u8 role, u16 apkVersionCode (0), u32 appUid (10000)
    hello_payload = struct.pack(">BBIi", PROTO_VERSION, ROLE_CONTROL, 3, 10000)
    send_frame(control_sock, T_HELLO, hello_payload)
    
    ack_type, ack_payload = recv_frame(control_sock)
    if ack_type != T_HELLO_ACK:
        print(f"[LỖI] Helper từ chối kết nối (mtype={ack_type})")
        return
    print("  -> Kết nối kênh điều khiển thành công!")

    # 3. Request Display on Control channel
    print(f"[3/4] Yêu cầu khởi tạo màn hình ô tô ({width}x{height} @ {fps}fps)...")
    # START_DISPLAY: width u16, height u16, dpi u16, fps u8, bitrate u32
    start_payload = struct.pack(">HHHBI", width, height, dpi, fps, bitrate)
    send_frame(control_sock, T_START_DISPLAY, start_payload)

    # 4. Connect Video Channel
    video_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    time.sleep(0.3)
    video_sock.connect(("127.0.0.1", HELPER_PORT))
    video_hello = struct.pack(">BBIi", PROTO_VERSION, ROLE_VIDEO, 3, 10000)
    send_frame(video_sock, T_HELLO, video_hello)
    
    ack_type, ack_payload = recv_frame(video_sock)
    if ack_type != T_HELLO_ACK:
        print(f"[LỖI] Kênh video bị từ chối")
        return
    print("  -> Kết nối kênh Video H.264 thành công!")

    # 5. Launch ffplay to display H.264 stream
    print("[4/4] Đang mở cửa sổ hiển thị màn hình ô tô (ffplay)...")
    ffplay_cmd = [
        "ffplay",
        "-loglevel", "quiet",
        "-flags", "low_delay",
        "-framedrop",
        "-fflags", "nobuffer",
        "-window_title", f"CarDroidVN - Màn hình xe CarPlay ({width}x{height})",
        "-x", str(min(width, 1280)),
        "-y", str(min(height, 720)),
        "-i", "-"
    ]

    try:
        player = subprocess.Popen(ffplay_cmd, stdin=subprocess.PIPE)
    except Exception as e:
        print(f"[LỖI] Không thể mở ffplay: {e}")
        print("Vui lòng đảm bảo ffplay đã có sẵn trên máy.")
        return

    # Loop to forward H.264 stream to ffplay
    ANNEX_B_START = b"\x00\x00\x00\x01"
    try:
        while True:
            mtype, payload = recv_frame(video_sock)
            if mtype == T_VIDEO_CONFIG:
                # spsLen u16, sps, ppsLen u16, pps
                sps_len, = struct.unpack_from(">H", payload, 0)
                sps = payload[2:2 + sps_len]
                pps_len, = struct.unpack_from(">H", payload, 2 + sps_len)
                pps = payload[4 + sps_len:4 + sps_len + pps_len]
                player.stdin.write(ANNEX_B_START + sps + ANNEX_B_START + pps)
                player.stdin.flush()
            elif mtype == T_VIDEO_FRAME:
                # ptsUs i64, flags u8, data Annex-B
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
    w = 1280
    h = 720
    if len(sys.argv) > 1 and sys.argv[1] == "--ultrawide":
        w = 1920
        h = 720
    run_laptop_test(width=w, height=h)
