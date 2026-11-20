"""Generate the versioned BlueNet local load-test Grafana dashboard."""

from __future__ import annotations

import json
from pathlib import Path

OUTPUT = Path(__file__).resolve().parent / "dashboards/bluenet-load-test.json"
DATASOURCE = {"type": "prometheus", "uid": "bluenet-prometheus"}


def panel(identifier, title, expressions, x, y, unit="short", height=8):
    if isinstance(expressions, str):
        expressions = [expressions]
    return {
        "id": identifier,
        "title": title,
        "type": "timeseries",
        "datasource": DATASOURCE,
        "gridPos": {"h": height, "w": 12, "x": x, "y": y},
        "fieldConfig": {
            "defaults": {"unit": unit, "custom": {"drawStyle": "line", "lineWidth": 1}},
            "overrides": [],
        },
        "options": {"legend": {"displayMode": "list", "placement": "bottom"}, "tooltip": {"mode": "multi"}},
        "targets": [
            {"refId": chr(65 + index), "expr": expression, "legendFormat": legend}
            for index, (expression, legend) in enumerate(expressions)
        ],
    }


def pair(expressions):
    return list(zip(expressions, ["{{pod}}", "{{uri}}", "{{job}}", "{{queue}}"]))


def main():
    definitions = [
        ("Scrape target health", "sum by (job) (up)", "short"),
        ("Pod CPU usage", '(sum by (namespace, pod) (rate(container_cpu_usage_seconds_total{job="kubernetes-resource", namespace="$namespace", pod=~"$pod", container!=""}[$__rate_interval]))) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "cores"),
        ("Pod memory working set", '(sum by (namespace, pod) (container_memory_working_set_bytes{job="kubernetes-resource", namespace="$namespace", pod=~"$pod", container!=""})) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "bytes"),
        ("Container restarts", 'sum by (pod, container) (kube_pod_container_status_restarts_total{namespace="$namespace", pod=~"$pod"})', "short"),
        ("Last termination: OOMKilled", 'sum by (pod, container) (kube_pod_container_status_last_terminated_reason{namespace="$namespace", pod=~"$pod", reason="OOMKilled"})', "short"),
        ("API HTTP request rate", '(sum by (namespace, pod, uri) (rate(http_server_requests_seconds_count{service="bluenet-api", namespace="$namespace"}[$__rate_interval]))) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "reqps"),
        ("API HTTP P95", '(histogram_quantile(0.95, sum by (le, namespace, pod, uri) (rate(http_server_requests_seconds_bucket{service="bluenet-api", namespace="$namespace"}[$__rate_interval])))) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "s"),
        ("API HTTP P99", '(histogram_quantile(0.99, sum by (le, namespace, pod, uri) (rate(http_server_requests_seconds_bucket{service="bluenet-api", namespace="$namespace"}[$__rate_interval])))) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "s"),
        ("API HTTP 5xx ratio", '(sum by (namespace, pod) (rate(http_server_requests_seconds_count{service="bluenet-api", namespace="$namespace", status=~"5.."}[$__rate_interval])) / clamp_min(sum by (namespace, pod) (rate(http_server_requests_seconds_count{service="bluenet-api", namespace="$namespace"}[$__rate_interval])), 1e-9)) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "percentunit"),
        ("JVM heap used", '(sum by (namespace, pod) (jvm_memory_used_bytes{namespace="$namespace", area="heap"})) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "bytes"),
        ("JVM non-heap used", '(sum by (namespace, pod) (jvm_memory_used_bytes{namespace="$namespace", area="nonheap"})) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "bytes"),
        ("JVM mean GC pause", '((sum by (namespace, pod) (rate(jvm_gc_pause_seconds_sum{namespace="$namespace"}[$__rate_interval]))) / clamp_min(sum by (namespace, pod) (rate(jvm_gc_pause_seconds_count{namespace="$namespace"}[$__rate_interval])), 1e-9)) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "s"),
        ("JVM live threads", '(jvm_threads_live_threads{namespace="$namespace"}) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "short"),
        ("JVM direct buffer used", '(sum by (namespace, pod) (jvm_buffer_memory_used_bytes{namespace="$namespace", id="direct"})) and on(namespace, pod) kube_pod_info{namespace="$namespace"}', "bytes"),
        ("RabbitMQ messages ready", 'rabbitmq_queue_messages_ready{queue=~"algorithm.judge.(run|formal)"}', "short"),
        ("RabbitMQ messages unacknowledged", 'rabbitmq_queue_messages_unacked{queue=~"algorithm.judge.(run|formal)"}', "short"),
        ("Algorithm exchange publish rate", 'sum(rate(rabbitmq_channel_messages_published_total{exchange="algorithm.judge"}[$__rate_interval]))', "msgps"),
        ("RabbitMQ global acknowledgement rate", 'sum by (protocol) (rate(rabbitmq_global_messages_acknowledged_total[$__rate_interval]))', "msgps"),
        ("PostgreSQL connections by state", 'sum by (state) (pg_stat_activity_count{datname="db_blue_net"})', "short"),
        ("PostgreSQL locks by mode", 'sum by (mode) (pg_locks_count{datname="db_blue_net"})', "short"),
        ("PostgreSQL transaction rate", ['sum(rate(pg_stat_database_xact_commit_total{datname="db_blue_net"}[$__rate_interval]))', 'sum(rate(pg_stat_database_xact_rollback_total{datname="db_blue_net"}[$__rate_interval]))'], "ops"),
        ("Redis memory used", "redis_memory_used_bytes", "bytes"),
        ("Redis connected clients", "redis_connected_clients", "short"),
        ("Redis keyspace hit ratio", 'rate(redis_keyspace_hits_total[$__rate_interval]) / clamp_min(rate(redis_keyspace_hits_total[$__rate_interval]) + rate(redis_keyspace_misses_total[$__rate_interval]), 1e-9)', "percentunit"),
    ]
    panels = []
    for index, (title, expressions, unit) in enumerate(definitions, start=1):
        if isinstance(expressions, list):
            paired = list(zip(expressions, ["commit/s", "rollback/s"]))
        else:
            paired = [(expressions, "{{pod}} {{uri}} {{job}} {{queue}} {{state}} {{mode}}")]
        row = (index - 1) // 2
        panels.append(panel(index, title, paired, 0 if index % 2 else 12, row * 8, unit))

    dashboard = {
        "uid": "bluenet-local-load-test",
        "title": "BlueNet Local Load Test",
        "description": "System metrics only. Use k6 JSONL/report for business throughput and valid completed workflows.",
        "timezone": "Asia/Shanghai",
        "schemaVersion": 39,
        "version": 1,
        "refresh": "15s",
        "time": {"from": "now-30m", "to": "now"},
        "templating": {
            "list": [
                {
                    "name": "namespace",
                    "type": "query",
                    "datasource": DATASOURCE,
                    "query": "label_values(container_memory_working_set_bytes, namespace)",
                    "current": {"text": "bluenet", "value": "bluenet"},
                    "refresh": 2,
                },
                {
                    "name": "pod",
                    "type": "query",
                    "datasource": DATASOURCE,
                    "query": 'label_values(kube_pod_info{namespace="$namespace"}, pod)',
                    "includeAll": True,
                    "multi": True,
                    "current": {"text": "All", "value": "$__all"},
                    "refresh": 2,
                },
            ]
        },
        "panels": panels,
    }
    OUTPUT.write_text(json.dumps(dashboard, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {OUTPUT} with {len(panels)} panels")


if __name__ == "__main__":
    main()
