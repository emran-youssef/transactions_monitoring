
CREATE TABLE outbox_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    aggregate_type  VARCHAR(64)     NOT NULL,
    aggregate_id    VARCHAR(64)     NOT NULL,
    event_type      VARCHAR(64)     NOT NULL,
    payload         TEXT            NOT NULL,
    created_at      TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published       BOOLEAN         NOT NULL DEFAULT FALSE,
    published_at    TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_outbox_events_published ON outbox_events (published, created_at);
