from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from monitoring.processing import parse_mib, sanitize_text, summarize_containers


class MonitoringProcessingTest(unittest.TestCase):
    def test_parses_docker_memory_units(self):
        self.assertAlmostEqual(parse_mib("512MiB"), 512.0)
        self.assertAlmostEqual(parse_mib("1GiB"), 1024.0)
        self.assertAlmostEqual(parse_mib("100kB"), 100 / 1024)
        self.assertIsNone(parse_mib("missing"))

    def test_summary_marks_restarts_oom_and_missing_samples(self):
        rows = [
            {"service": "api", "memory_mib": 100, "oom": False, "restarts": 0, "phase": "idle"},
            {"service": "api", "memory_mib": 400, "oom": True, "restarts": 1, "phase": "steady"},
            {"service": "api", "memory_mib": None, "error": "stats failed", "phase": "recovery"},
        ]
        summary = summarize_containers(rows)["api"]
        self.assertEqual(summary["peak_mib"], 400)
        self.assertEqual(summary["idle_mib"], 100)
        self.assertTrue(summary["oom"])
        self.assertEqual(summary["max_restarts"], 1)
        self.assertEqual(summary["missing_samples"], 1)
        self.assertFalse(summary["complete"])

    def test_redacts_sensitive_values(self):
        text = 'password=secret cookie=abc "csrfToken":"token" uploadUrl=http://signed'
        redacted = sanitize_text(text)
        self.assertNotIn("secret", redacted)
        self.assertNotIn("abc", redacted)
        self.assertNotIn("token", redacted)
        self.assertNotIn("http://signed", redacted)


if __name__ == "__main__":
    unittest.main()
