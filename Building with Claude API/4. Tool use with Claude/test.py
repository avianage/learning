import unittest
import math
from main import calculate_pi, greeting
from io import StringIO
import sys


class TestCalculatePi(unittest.TestCase):

    def test_pi_default_5_digits(self):
        """Pi calculated to 5 decimal places should match math.pi rounded to 5 places."""
        result = calculate_pi()
        expected = round(math.pi, 5)  # 3.14159
        self.assertEqual(result, expected, f"Expected {expected}, got {result}")

    def test_pi_returns_float(self):
        """calculate_pi() should return a float."""
        result = calculate_pi()
        self.assertIsInstance(result, float)

    def test_pi_correct_value(self):
        """Pi to 5 digits should equal 3.14159."""
        result = calculate_pi()
        self.assertEqual(result, 3.14159)

    def test_pi_fewer_digits(self):
        """calculate_pi() should correctly round to fewer decimal places."""
        self.assertEqual(calculate_pi(2), round(math.pi, 2))  # 3.14
        self.assertEqual(calculate_pi(0), round(math.pi, 0))  # 3.0

    def test_pi_more_digits(self):
        """calculate_pi() with more digits should still be close to math.pi."""
        result = calculate_pi(6)
        self.assertAlmostEqual(result, math.pi, places=5)

    def test_pi_within_tolerance(self):
        """Pi should be within a small tolerance of the true value."""
        result = calculate_pi(5)
        self.assertAlmostEqual(result, math.pi, places=5)


class TestGreeting(unittest.TestCase):

    def test_greeting_output(self):
        """greeting() should print 'Hi There' to stdout."""
        captured = StringIO()
        sys.stdout = captured
        greeting()
        sys.stdout = sys.__stdout__
        self.assertEqual(captured.getvalue().strip(), "Hi There")


if __name__ == "__main__":
    unittest.main()
