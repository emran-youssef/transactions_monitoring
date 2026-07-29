package com.eyatrooz.transaction_monitoring.case_management_service.kafka;

public final class KafkaTopics {
    public static final String CASE_CREATED = "cases.created.v1";
    public static final String CASE_UPDATED = "cases.updated.v1";
    public static final String FLAGGED_TRANSACTION = "transactions.flagged.v1";

    private KafkaTopics() { }  // prevents instantiation — this class is just a constants holder
}


