CREATE TABLE transactions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    account_id      VARCHAR(64)     NOT NULL,
    amount          DECIMAL(19, 4)  NOT NULL,
    currency        VARCHAR(3),
    transaction_type VARCHAR(32)    NOT NULL,
    counterparty    VARCHAR(128),
    status          VARCHAR(32)     NOT NULL,
    created_at      TIMESTAMP(6)    NOT NULL,
    received_at     TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);