ALTER TABLE rule_evaluations
    ADD CONSTRAINT fk_rule_eval_transaction_history
    FOREIGN KEY (transaction_id) REFERENCES transaction_history (transaction_id);