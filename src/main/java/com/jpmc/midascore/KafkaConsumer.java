package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaConsumer {

    private List<Transaction> receivedTransactions = new ArrayList<>();

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-group-fresh",  // ← CHANGED THIS
            properties = {
                    "auto.offset.reset=earliest"  // ← ADDED THIS
            }
    )
    public void listen(Transaction transaction) {
        receivedTransactions.add(transaction);
        System.out.println("Received transaction: " + transaction);

        // Set breakpoint HERE on line 18
        if (receivedTransactions.size() <= 4) {
            System.out.println("Transaction #" + receivedTransactions.size() +
                    " Amount: " + transaction.getAmount());
        }
    }

    public List<Transaction> getReceivedTransactions() {
        return receivedTransactions;
    }
}