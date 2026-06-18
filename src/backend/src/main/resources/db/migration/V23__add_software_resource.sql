CREATE TABLE IF NOT EXISTS tb_software_resource (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    direction VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    external_url VARCHAR(2048) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'active'
);

CREATE INDEX idx_software_resource_direction_status ON tb_software_resource(direction, status);
CREATE INDEX idx_software_resource_status_sort ON tb_software_resource(status, sort_order);
