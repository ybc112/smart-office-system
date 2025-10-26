import utime as time
from micropython import const
import ustruct as struct

AHT10_ADDR               = const(0x38)
AHT10_CALIBRATION_CMD    = const(0xE1)
AHT10_NORMAL_CMD         = const(0xA8)
AHT10_GET_DATA           = const(0xAC)


class AHT10:
    def __init__(self, i2c, address=AHT10_ADDR):
        self.i2c = i2c
        self.address = address

    def init(self):
        self.sensor_init()

    def sensor_init(self):
        # Use explicit writeto to improve compatibility with SoftI2C
        self.i2c.writeto(self.address, bytes([AHT10_NORMAL_CMD, 0x00, 0x00]))
        time.sleep_ms(350)
        self.i2c.writeto(self.address, bytes([AHT10_CALIBRATION_CMD, 0x08, 0x00]))
        time.sleep_ms(450)

    def _is_calibration_enabled(self):
        status = self.i2c.readfrom(self.address, 1)
        status_hex = struct.unpack_from(">b", status)
        return (status_hex[0] & int('0x68', 16)) == int('0x08', 16)

    def read_temperature(self):
        # Trigger measurement
        self.i2c.writeto(self.address, bytes([AHT10_GET_DATA, 0x00, 0x00]))
        if self._is_calibration_enabled():
            temp = self.i2c.readfrom(self.address, 6)
            temp_hex = struct.unpack(">BBBBBB", temp)
            cur_temp = ((temp_hex[3] & 0xF) << 16 | temp_hex[4] << 8 | temp_hex[5]) * 200.0 / (1 << 20) - 50
            return cur_temp
        else:
            self.sensor_init()
            raise OSError("AHT10 not calibrated")

    def read_humidity(self):
        # Trigger measurement
        self.i2c.writeto(self.address, bytes([AHT10_GET_DATA, 0x00, 0x00]))
        if self._is_calibration_enabled():
            temp = self.i2c.readfrom(self.address, 6)
            temp_hex = struct.unpack(">BBBBBB", temp)
            # Ensure non-zero middle byte
            while temp_hex[2] == 0:
                temp = self.i2c.readfrom(self.address, 6)
                temp_hex = struct.unpack(">BBBBBB", temp)
            cur_humi = (temp_hex[1] << 12 | temp_hex[2] << 4 | (temp_hex[3] & 0xF0) >> 4) * 100.0 / (1 << 20)
            return cur_humi
        else:
            self.sensor_init()
            raise OSError("AHT10 not calibrated")

    def read(self):
        # Returns (temperature, humidity)
        t = self.read_temperature()
        h = self.read_humidity()
        return (t, h)