package com.eyatrooz.transaction_monitoring.audit_service.kafka;

public final class KafkaTopics {
    public static final String TRANSACTION_CREATED = "transactions.created.v1";
    public static final String FLAGGED_TRANSACTION = "transactions.flagged.v1";
    public static final String CASE_CREATED = "cases.created.v1";
    public static final String CASE_UPDATED = "cases.updated.v1";

    // prevents instantiation — this class is just a constants holder
    private KafkaTopics() { }
}
