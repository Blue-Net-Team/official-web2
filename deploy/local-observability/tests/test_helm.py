from __future__ import annotations

import subprocess
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[3]
CHART = ROOT / "deploy/charts/bluenet"


def render(service: str, production: bool = False) -> list[dict]:
    values = CHART / "values" / ("api.yaml" if production else f"local/{service}.yaml")
    result = subprocess.run(
        ["helm", "template", f"bluenet-{service}", str(CHART), "-n", "bluenet", "-f", str(values)],
        cwd=ROOT,
        check=True,
        capture_output=True,
        text=True,
    )
    return [document for document in yaml.safe_load_all(result.stdout) if document]


def find_kind(documents: list[dict], kind: str) -> dict:
    return next(document for document in documents if document["kind"] == kind)


class LocalObservabilityHelmTest(unittest.TestCase):
    def test_api_management_port_is_not_a_service(self):
        documents = render("api")
        deployment = find_kind(documents, "Deployment")
        service = find_kind(documents, "Service")
        pod = deployment["spec"]["template"]
        container = pod["spec"]["containers"][0]

        self.assertEqual(deployment["spec"]["replicas"], 1)
        self.assertEqual(pod["metadata"]["annotations"]["prometheus.io/port"], "8081")
        self.assertIn({"name": "management", "containerPort": 8081, "protocol": "TCP"}, container["ports"])
        self.assertTrue(all(port.get("nodePort") != 8081 for port in service["spec"]["ports"]))

    def test_judge_remains_cluster_ip_with_separate_management_port(self):
        documents = render("judge")
        deployment = find_kind(documents, "Deployment")
        service = find_kind(documents, "Service")
        container = deployment["spec"]["template"]["spec"]["containers"][0]

        self.assertEqual(deployment["spec"]["replicas"], 2)
        self.assertEqual(service["spec"]["type"], "ClusterIP")
        self.assertEqual(service["spec"]["ports"], [{"name": "http", "port": 8090, "targetPort": 8090, "protocol": "TCP"}])
        self.assertIn({"name": "management", "containerPort": 8091, "protocol": "TCP"}, container["ports"])

    def test_production_values_do_not_enable_prometheus(self):
        for values_name in ("api.yaml", "judge.yaml"):
            values = yaml.safe_load((CHART / "values" / values_name).read_text(encoding="utf-8"))
            env = values["env"]
            self.assertFalse(any(item["name"].startswith("MANAGEMENT_") for item in env))
            self.assertNotIn("prometheus.io/scrape", values.get("podAnnotations", {}))


if __name__ == "__main__":
    unittest.main()
