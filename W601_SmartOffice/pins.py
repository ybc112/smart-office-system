# Central GPIO pin mapping for RT-Thread MicroPython (W601)
# Use this to convert numeric pins to the required ("GPIO_x", pin#) tuple.

_PIN_MAP = {
    # I2C pins
    23: ("GPIO_A", 0),   # PA0 (I2C1_SCL)
    24: ("GPIO_A", 1),   # PA1 (I2C_SDA)

    # RGB LED pins
    30: ("GPIO_A", 13),  # PA13 (LED_R)
    31: ("GPIO_A", 14),  # PA14 (LED_G)
    32: ("GPIO_A", 15),  # PA15 (LED_B)
    # Buzzer
    45: ("GPIO_B", 15),  # PB15 (BUZZER)
    # Flame sensor (external)
    65: ("GPIO_B", 10),  # PB10 (FLAME)
    # ADC pin for external light sensor
    7: ("GPIO_B", 25),   # PB25/SAR-ADC7 (LIGHT_ADC)
}


def pin_id(pin):
    """Return RT-Thread Pin id tuple for a given numeric pin or already-correct tuple.
    - If pin is a tuple like ("GPIO_A", 13), return as is.
    - If pin is a label+number tuple like ("scl1", 23), convert by numeric part.
    - If pin is an int and present in _PIN_MAP, return mapped tuple.
    - Otherwise, raise ValueError to prompt updating the map.
    """
    # Already in expected tuple format
    if isinstance(pin, tuple):
        if len(pin) == 2 and isinstance(pin[0], str) and pin[0].startswith("GPIO_"):
            return pin
        # Try to convert label+number tuples by numeric part
        try:
            return _PIN_MAP[pin[1]]
        except Exception:
            raise ValueError("Unknown labeled pin {}; update _PIN_MAP".format(pin))

    # Numeric mapping
    if isinstance(pin, int):
        try:
            return _PIN_MAP[pin]
        except Exception:
            raise ValueError("Unknown pin id {}; update _PIN_MAP".format(pin))

    # Unrecognized type
    raise ValueError("Unsupported pin type {}; expected int or tuple".format(type(pin)))