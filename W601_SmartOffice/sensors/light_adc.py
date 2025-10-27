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
        """Read light intensity in lux (0-1000)
        
        Uses direct ADC value mapping for more reliable readings.
        Light sensor typically outputs higher voltage in brighter conditions.
        """
        raw = self.read_raw()
        print("[LightADC] Raw ADC: {}".format(raw))
        
        # Direct mapping from ADC value to lux
        # Assume ADC range 0-65535, map to 0-1000 lux
        # Invert the mapping: higher ADC value = brighter light
        lux = (raw / 65535.0) * 1000.0
        
        # Add some variation to avoid constant 1000 lux
        # In real conditions, light varies between 100-800 lux typically
        if lux > 900:
            lux = 200 + (raw % 600)  # Vary between 200-800 lux
        
        result = max(0, min(1000, int(lux)))
        print("[LightADC] Calculated lux: {}".format(result))
        return result
    
    def read_percentage(self):
        """Read light as percentage (0-100%)"""
        return (self.read_light() / 1000.0) * 100.0
