import importlib.util
import math
import unittest
from pathlib import Path


MODULE_PATH = Path(__file__).parents[1] / "setup" / "measure_algorithm_limits.py"
SPEC = importlib.util.spec_from_file_location("measure_algorithm_limits", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class MeasureAlgorithmLimitsTest(unittest.TestCase):
    def test_uses_ceiling_of_max_plus_one_hundred_ms(self):
        self.assertEqual(MODULE.measured_limit_ms(81.01), 182)
        self.assertEqual(MODULE.measured_limit_ms(80), 180)
        self.assertEqual(MODULE.measured_limit_ms(0.01, extra_ms=50), 51)

    def test_supports_explicit_extra_ms_without_p95_or_rounding(self):
        self.assertEqual(MODULE.measured_limit_ms(123.4, extra_ms=20), 144)

    def test_rejects_missing_or_invalid_benchmark(self):
        for value in (None, "", float("nan"), float("inf"), -1):
            with self.subTest(value=value), self.assertRaises(ValueError):
                MODULE.measured_limit_ms(value)

    def test_requires_done_python_benchmark_for_every_question(self):
        context = {"algorithms": [{"questionId": 1, "benchmark": {"status": "PENDING", "maxTimeMs": 80}}]}
        with self.assertRaisesRegex(ValueError, "benchmark.*DONE"):
            MODULE.validated_measurements(context)

    def test_collects_max_and_derived_limit_without_confirming(self):
        context = {
            "algorithms": [{
                "questionId": 1,
                "benchmark": {"status": "DONE", "maxTimeMs": 81.2, "p95TimeMs": 70},
            }],
        }
        self.assertEqual(MODULE.validated_measurements(context), [(1, 81.2, 182)])


if __name__ == "__main__":
    unittest.main()
