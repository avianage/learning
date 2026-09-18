def greeting():
    print("Hi There")


def calculate_pi(digits=5):
    """
    Calculate Pi to the specified number of decimal places.
    Uses the Leibniz formula for Pi:
      π/4 = 1 - 1/3 + 1/5 - 1/7 + 1/9 - ...
    A high number of iterations is used to ensure accuracy up to 5 decimal places.

    Args:
        digits (int): The number of decimal places to return. Defaults to 5.

    Returns:
        float: Pi rounded to the specified number of decimal places.
    """
    numerator = 4.0
    denominator = 1.0
    operation = 1.0
    pi = 0.0
    iterations = 1_000_000  # More iterations = more precision

    for _ in range(iterations):
        pi += operation * (numerator / denominator)
        denominator += 2.0
        operation *= -1.0  # Alternate between adding and subtracting

    return round(pi, digits)