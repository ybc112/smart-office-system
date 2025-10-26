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
        """Read voltage (W601 calibrated)"""
        raw = self.read_raw()
        # Based on W601 official example calibration
        # value = (adc.read() - 8192.0) / 8192 * 2.25 / 1.2 + 1.584
        return (raw - 8192.0) / 8192.0 * 2.25 / 1.2 + 1.584
    
    def read_light(self):
        """Read light intensity in lux (0-1000)
        
        This is a simplified conversion. For accurate readings,
        calibrate based on your specific photoresistor characteristics.
        """
        voltage = self.read_voltage()
        # Simple linear mapping: 0V = 0 lux, ~3.3V = 1000 lux
        # Adjust this formula based on your sensor's response curve
        lux = (voltage / 3.3) * 1000.0
        return max(0, min(1000, lux))  # Clamp to 0-1000 range
    
    def read_percentage(self):
        """Read light as percentage (0-100%)"""
        return (self.read_light() / 1000.0) * 100.0
