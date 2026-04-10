## Purpose

API statistics and monitoring dashboard for tracking request volume trends, endpoint access patterns, and performance metrics.

## Requirements

### Requirement: Request volume trend query
The system SHALL provide an API endpoint that returns request volume aggregated by time intervals for a given time range.

#### Scenario: Query trends for last 24 hours
- **WHEN** client sends `GET /api/v1/admin/audit/statistics/trends?period=24h`
- **THEN** system SHALL return an array of time buckets, each containing `time` (timestamp) and `count` (number of requests)
- **THEN** time buckets SHALL be grouped by hour (24 data points)
- **THEN** time range SHALL be the last 24 hours from current time

#### Scenario: Query trends for last 7 days
- **WHEN** client sends `GET /api/v1/admin/audit/statistics/trends?period=7d`
- **THEN** system SHALL return time buckets grouped by day (7 data points)

#### Scenario: Query trends for last 30 days
- **WHEN** client sends `GET /api/v1/admin/audit/statistics/trends?period=30d`
- **THEN** system SHALL return time buckets grouped by day (30 data points)

### Requirement: Endpoint access ranking query
The system SHALL provide an API endpoint that returns endpoints ranked by request count.

#### Scenario: Query top endpoints by access count
- **WHEN** client sends `GET /api/v1/admin/audit/statistics/endpoints?period=7d&limit=20`
- **THEN** system SHALL return an array of endpoints sorted by request count in descending order
- **THEN** each entry SHALL contain `pattern` (URI pattern), `count` (total requests), `avgDurationMs` (average response time), `errorCount` (failed requests)
- **THEN** results SHALL be limited to the specified `limit` (default 20)
- **THEN** results SHALL be filtered to the specified `period`

#### Scenario: Default parameters
- **WHEN** client sends `GET /api/v1/admin/audit/statistics/endpoints` without parameters
- **THEN** system SHALL use default values: `period=7d`, `limit=20`

### Requirement: Endpoint latency ranking query
The system SHALL provide an API endpoint that returns endpoints ranked by average response time.

#### Scenario: Query top slowest endpoints
- **WHEN** client sends `GET /api/v1/admin/audit/statistics/latency?period=7d&limit=20`
- **THEN** system SHALL return an array of endpoints sorted by average response time in descending order
- **THEN** each entry SHALL contain `pattern` (URI pattern), `avgDurationMs` (average response time), `maxDurationMs` (max response time), `count` (total requests)
- **THEN** results SHALL be limited to the specified `limit` (default 20)

### Requirement: Statistics API access control
All statistics endpoints SHALL require admin-level permission.

#### Scenario: Unauthenticated access to statistics
- **WHEN** unauthenticated client requests any statistics endpoint
- **THEN** system SHALL return HTTP 401

#### Scenario: Non-admin access to statistics
- **WHEN** authenticated non-admin user requests any statistics endpoint
- **THEN** system SHALL return HTTP 403

### Requirement: Monitoring dashboard page
The system SHALL provide a monitoring dashboard at `/admin/panel` in the admin interface.

#### Scenario: Dashboard displays request volume trend chart
- **WHEN** admin navigates to `/admin/panel`
- **THEN** page SHALL display a line chart showing request volume over time
- **THEN** chart SHALL support switching between 24h, 7d, and 30d periods

#### Scenario: Dashboard displays endpoint access ranking
- **WHEN** admin navigates to `/admin/panel`
- **THEN** page SHALL display a table showing top 20 endpoints by request count
- **THEN** table columns SHALL include: URI pattern, request count, average response time, error count

#### Scenario: Dashboard displays endpoint latency ranking
- **WHEN** admin navigates to `/admin/panel`
- **THEN** page SHALL display a table showing top 20 endpoints by average response time
- **THEN** table columns SHALL include: URI pattern, average response time (ms), max response time (ms), request count
