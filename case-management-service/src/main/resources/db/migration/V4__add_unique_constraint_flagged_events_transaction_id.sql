-- Replaces the non-unique index with a unique constraint so the DB enforces the
-- idempotency guarantee that CaseCreationService relies on when handling
-- duplicate/redelivered flagged-transaction Kafka messages.
DROP INDEX idx_flagged_events_transaction_id ON flagged_transaction_events;
CREATE UNIQUE INDEX idx_flagged_events_transaction_id ON flagged_transaction_events (transaction_id);
