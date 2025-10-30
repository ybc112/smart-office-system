# External Light Sensor (ADC Analog Input)
# For photoresistor or other analog light sensors

from machine import ADC
import config

class LightADC:
    """External analog light sensor using ADC input"""
    
    def __init__(self):
        """Initialize ADC light sensor"""
        try:
            # Initialize ADC using W601 device name + channel
            self.adc = ADC("adc", config.LIGHT_ADC_PIN)
            print("[LightADC] Initialized on channel {}".format(config.LIGHT_ADC_PIN))
        except Exception as e:
            print("[LightADC] Init failed: {}".format(e))
            self.adc = None
    
    def read_raw(self):
        """Read raw ADC value"""
        if self.adc is None:
            return 0
        try:
            return self.adc.read()
        except Exception as e:
            print("[LightADC] Read failed: {}".format(e))
            return 0
    
    def read_voltage(self):
        """Read voltage (simplified for W601)"""
        raw = self.read_raw()
        # Simplified voltage calculation: assume 16-bit ADC with 3.3V reference
        # ADC range: 0-65535 maps to 0-3.3V
        return (raw / 65535.0) * 3.3
    
    def read_light(self):
        """Read light intensity in lux (0-1000)"""
        raw = self.read_raw()
        print("[LightADC] Raw ADC: {}".format(raw))

        # Use configurable calibration range
        min_raw = getattr(config, 'LIGHT_ADC_MIN_RAW', 0)
        max_raw = getattr(config, 'LIGHT_ADC_MAX_RAW', 65535)
        if max_raw <= min_raw:
            max_raw = 65535
            min_raw = 0

        # Clamp and normalize
        if raw < min_raw:
            raw = min_raw
        elif raw > max_raw:
            raw = max_raw
        norm = (raw - min_raw) / float(max_raw - min_raw)

        # Optional inversion to match wiring (LDR top/bottom of divider)
        if getattr(config, 'LIGHT_ADC_INVERT', False):
            norm = 1.0 - norm

        lux = norm * 1000.0
        
        # Ensure result is within valid range
        result = max(0, min(1000, int(lux)))
        print("[LightADC] Calculated lux: {}".format(result))
        return result
    
    def read_percentage(self):
        """Read light as percentage (0-100%)"""
        return (self.read_light() / 1000.0) * 100.0
