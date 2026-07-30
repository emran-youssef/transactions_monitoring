CREATE TABLE audit_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    event_id     VARCHAR(36)  NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    entity_id    VARCHAR(100) NOT NULL,
    occurred_at  TIMESTAMP    NOT NULL,
    recorded_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload      JSON         NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_audit_log_event_id UNIQUE (event_id)
) ENGINE=InnoDB;

CREATE INDEX idx_audit_log_entity_id ON audit_log (entity_id);
CREATE INDEX idx_audit_log_event_type ON audit_log (event_type);
