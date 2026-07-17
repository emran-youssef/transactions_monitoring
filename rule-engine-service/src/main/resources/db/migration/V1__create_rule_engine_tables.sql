CREATE TABLE transaction_history (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    transaction_id    BIGINT          NOT NULL,
    account_id        VARCHAR(64)     NOT NULL,
    amount            DECIMAL(19, 4)  NOT NULL,
    transaction_type  VARCHAR(32)     NOT NULL,
    created_at        TIMESTAMP(6)    NOT NULL,
    received_at       TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Speeds up queries that find all transactions for a specific account (used by Velocity rule)
CREATE INDEX idx_txn_history_account_id ON transaction_history (account_id);
-- Speeds up time-based queries, such as finding recent transactions within a time window
CREATE INDEX idx_txn_history_created_at ON transaction_history (created_at);
-- Prevents duplicate transaction events and supports idempotent event processing
CREATE UNIQUE INDEX uq_txn_history_transaction_id ON transaction_history (transaction_id);


-- TABLE ROLE:  stores the overall evaluation result for each transaction
CREATE TABLE rule_evaluations (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    transaction_id      BIGINT          NOT NULL,
    account_id          VARCHAR(64)     NOT NULL,
    overall_risk_score  DECIMAL(6, 2)   NOT NULL,
    flagged             BOOLEAN         NOT NULL,
    evaluated_at        TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_rule_eval_account_id ON rule_evaluations (account_id);
CREATE UNIQUE INDEX uq_rule_eval_transaction_id ON rule_evaluations (transaction_id);


-- TABLE ROLE: stores the result of each individual rule executed during an evaluation
CREATE TABLE rule_evaluation_results (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    rule_evaluation_id  BIGINT          NOT NULL,
    rule_name           VARCHAR(32)     NOT NULL,
    triggered           BOOLEAN         NOT NULL,
    score               DECIMAL(6, 2)   NOT NULL,
    details             TEXT,

    PRIMARY KEY (id),
    CONSTRAINT fk_rule_eval_results_evaluation
        FOREIGN KEY (rule_evaluation_id) REFERENCES rule_evaluations (id)
) ENGINE=InnoDB;

CREATE INDEX idx_rule_eval_results_eval_id ON rule_evaluation_results (rule_evaluation_id);