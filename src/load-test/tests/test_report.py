import json
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from monitoring.report import sanitize_k6_summary, write_report


class ReportTest(unittest.TestCase):
    def test_removes_setup_data_and_recursively_redacts_secrets(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "summary.json"
            path.write_text(json.dumps({
                "setup_data": {"users": [{"password": "secret", "session": {"authToken": "jwt"}}]},
                "metrics": {"custom": {"count": 1}},
            }))
            sanitize_k6_summary(path)
            result = json.loads(path.read_text())
            self.assertNotIn("setup_data", result)
            self.assertEqual(result["metrics"]["custom"]["count"], 1)
            self.assertNotIn("secret", path.read_text())
            self.assertNotIn("jwt", path.read_text())

    def test_preserves_partial_results_and_undrained_queue(self):
        with tempfile.TemporaryDirectory() as directory:
            run_dir = Path(directory)
            (run_dir / "metadata.json").write_text(json.dumps({
                "runId": "partial",
                "scenario": "algorithm",
                "complete": False,
                "error": "job timed out",
            }))
            (run_dir / "containers.jsonl").write_text(
                json.dumps({"service": "judge", "memory_mib": 512, "oom": False, "restarts": 0, "phase": "steady"}) + "\n"
            )
            (run_dir / "jvm.jsonl").write_text(
                json.dumps({"service": "judge", "error": "actuator timeout"}) + "\n"
            )
            (run_dir / "queues.jsonl").write_text(
                json.dumps({"operation": "formal", "ready": 3, "unacked": 1}) + "\n"
            )

            report = write_report(run_dir)
            self.assertFalse(report["complete"])
            self.assertFalse(report["k6"]["complete"])
            self.assertEqual(report["queues"]["formal"]["final_ready"], 3)
            self.assertEqual(report["jvm"]["judge"]["missing_samples"], 1)
            self.assertTrue((run_dir / "report.md").is_file())


if __name__ == "__main__":
    unittest.main()
