package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final TransactionService transactionService;

    public KafkaConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-group-task5",
            properties = {
                    "auto.offset.reset=earliest"
            }
    )
    public void listen(Transaction transaction) {
        System.out.println("Received transaction: " + transaction);
        transactionService.processTransaction(transaction);
    }
}