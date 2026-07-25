
CREATE TABLE flagged_transaction_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    transaction_id  BIGINT          NOT NULL,
    account_id      VARCHAR(64)     NOT NULL,
    risk_score      DECIMAL(19, 4)  NOT NULL,
    flagged         BOOLEAN         NOT NULL,
    evaluated_at    TIMESTAMP(6)    NOT NULL,
    received_at     TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_flagged_events_transaction_id ON flagged_transaction_events (transaction_id);
CREATE INDEX idx_flagged_events_account_id ON flagged_transaction_events (account_id);


CREATE TABLE cases (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    transaction_id      BIGINT          NOT NULL,
    account_id          VARCHAR(64)     NOT NULL,
    overall_risk_score  DECIMAL(19, 4)  NOT NULL,
    status              VARCHAR(32)     NOT NULL,
    assigned_analyst    VARCHAR(64),
    created_at          TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE UNIQUE INDEX idx_cases_transaction_id ON cases(transaction_id);
CREATE INDEX idx_cases_account_id ON cases (account_id);
CREATE INDEX idx_cases_status ON cases (status);


CREATE TABLE case_history (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    case_id     BIGINT          NOT NULL,
    action      VARCHAR(32)     NOT NULL,
    analyst     VARCHAR(64),
    comment     VARCHAR(512),
    created_at  TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_case_history_case FOREIGN KEY (case_id) REFERENCES cases (id)
) ENGINE=InnoDB;