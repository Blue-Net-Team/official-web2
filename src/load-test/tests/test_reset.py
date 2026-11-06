from pathlib import Path
import sys
import unittest
from unittest.mock import patch
from click.testing import CliRunner

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from setup.reset import build_reset_commands, main, validate_reset_request


class ResetGuardTest(unittest.TestCase):
    def test_accepts_only_dedicated_project_and_matching_confirmations(self):
        request = validate_reset_request(
            project="bluenet-loadtest",
            env_name="loadtest",
            confirm_project="bluenet-loadtest",
            confirm_text="DELETE",
            docker_host="",
        )
        self.assertEqual(request.project, "bluenet-loadtest")
        self.assertEqual(request.env_name, "loadtest")

    def test_rejects_wrong_project(self):
        with self.assertRaisesRegex(ValueError, "dedicated"):
            validate_reset_request("other", "dev", "other", "DELETE", "")

    def test_accepts_explicitly_allowed_existing_local_environment(self):
        request = validate_reset_request("bluenet", "dev", "bluenet", "DELETE", "")
        self.assertEqual(request.project, "bluenet")
        self.assertEqual(request.env_name, "dev")

    def test_rejects_missing_or_mismatched_confirmation(self):
        with self.assertRaisesRegex(ValueError, "confirmation"):
            validate_reset_request("bluenet-loadtest", "loadtest", "", "DELETE", "")
        with self.assertRaisesRegex(ValueError, "confirmation"):
            validate_reset_request("bluenet-loadtest", "loadtest", "bluenet-loadtest", "NO", "")

    def test_rejects_remote_docker_host(self):
        with self.assertRaisesRegex(ValueError, "local Docker"):
            validate_reset_request(
                "bluenet-loadtest", "loadtest", "bluenet-loadtest", "DELETE", "ssh://prod"
            )

    def test_reset_down_includes_profiles_so_profiled_volumes_are_removed(self):
        from pathlib import Path

        request = validate_reset_request("bluenet", "dev", "bluenet", "DELETE", "")
        commands = build_reset_commands(Path("."), request)
        self.assertIn("--profile", commands[0])
        self.assertEqual(commands[0][commands[0].index("--profile") + 1], "full")
        self.assertIn("--volumes", commands[0])

    @patch("setup.reset.subprocess.run")
    def test_invalid_request_has_no_side_effects(self, run):
        result = CliRunner().invoke(
            main,
            [
                "--project",
                "bluenet-loadtest",
                "--env-name",
                "loadtest",
                "--confirm-project",
                "bluenet-loadtest",
            ],
            env={"LOADTEST_RESET_CONFIRM": "NO"},
        )
        self.assertEqual(result.exit_code, 2)
        run.assert_not_called()


if __name__ == "__main__":
    unittest.main()
