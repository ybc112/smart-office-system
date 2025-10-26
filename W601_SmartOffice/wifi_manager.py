import network
import utime as time
from config import WIFI_SSID, WIFI_PASSWORD, WIFI_RETRY_MS, DEBUG


class WiFiManager:
    def __init__(self, ssid=WIFI_SSID, password=WIFI_PASSWORD):
        self.wlan = network.WLAN(network.STA_IF)
        self.ssid = ssid
        self.password = password

    def connect(self):
        try:
            self.wlan.active(True)
            if DEBUG:
                print("WiFi activated")
                try:
                    scan_results = self.wlan.scan()
                    print("WiFi scan results:", len(scan_results), "networks found")
                    for ssid, bssid, channel, rssi, authmode, hidden in scan_results:
                        if ssid.decode() == self.ssid:
                            print("Target network found:", ssid.decode(), "RSSI:", rssi)
                except Exception as e:
                    print("WiFi scan error:", e)
            
            if not self.wlan.isconnected():
                if DEBUG:
                    print("Connecting to WiFi:", self.ssid)
                self.wlan.connect(self.ssid, self.password)
            
            # wait up to ~20s for DHCP (increased timeout for mobile hotspots)
            for i in range(100):
                if self.wlan.isconnected():
                    # Additional check: ensure we have a valid IP (not 0.0.0.0)
                    config = self.wlan.ifconfig()
                    if config[0] != '0.0.0.0':
                        if DEBUG:
                            print("WiFi connected after", i * 200, "ms")
                            print("Valid IP obtained:", config[0])
                        break
                    else:
                        if DEBUG and i % 25 == 0:  # Print every 5 seconds
                            print("Connected but waiting for valid IP... (", i * 200, "ms)")
                time.sleep_ms(200)
            
            connected = self.wlan.isconnected()
            if DEBUG:
                if connected:
                    config = self.wlan.ifconfig()
                    print("WiFi connection successful")
                    print("IP config:", config)
                    if config[0] == '0.0.0.0':
                        print("WARNING: Connected but IP is 0.0.0.0 - DHCP may have failed")
                        print("This is common with mobile hotspots - try using a router WiFi")
                else:
                    print("WiFi connection failed after timeout")
                    print("WiFi status:", self.wlan.status())
                    print("Suggestion: Check if hotspot uses 2.4GHz and WPA2 encryption")
            
            return connected
        except Exception as e:
            if DEBUG:
                print("WiFi connect error:", e)
            return False

    def ensure_connected(self):
        if self.is_connected():
            return True
        ok = self.connect()
        if not ok:
            time.sleep_ms(WIFI_RETRY_MS)
        return ok

    def is_connected(self):
        try:
            return bool(self.wlan.isconnected())
        except Exception:
            return False

    def disconnect(self):
        try:
            if self.wlan.isconnected():
                self.wlan.disconnect()
        except Exception:
            pass

    def ip(self):
        try:
            if self.wlan and self.wlan.isconnected():
                cfg = self.wlan.ifconfig()
                if cfg and len(cfg) > 0:
                    return cfg[0]
        except Exception:
            pass
        return None