package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    public TransactionService(UserRepository userRepository,
                              TransactionRecordRepository transactionRecordRepository,
                              RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public boolean processTransaction(Transaction transaction) {
        // Validate sender exists
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        if (senderOpt.isEmpty()) {
            System.out.println("Invalid sender ID: " + transaction.getSenderId());
            return false;
        }

        // Validate recipient exists
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());
        if (recipientOpt.isEmpty()) {
            System.out.println("Invalid recipient ID: " + transaction.getRecipientId());
            return false;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        // Validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            System.out.println("Insufficient balance for sender: " + sender.getName() +
                    " (balance: " + sender.getBalance() + ", amount: " + transaction.getAmount() + ")");
            return false;
        }

        // Get incentive from API
        float incentiveAmount = 0;
        try {
            Incentive incentive = restTemplate.postForObject(INCENTIVE_API_URL, transaction, Incentive.class);
            if (incentive != null) {
                incentiveAmount = incentive.getAmount();
                System.out.println("Incentive received: " + incentiveAmount);
            }
        } catch (Exception e) {
            System.err.println("Error calling incentive API: " + e.getMessage());
            // Continue processing even if incentive API fails
        }

        // Process the transaction
        // Deduct from sender
        sender.setBalance(sender.getBalance() - transaction.getAmount());

        // Add to recipient (transaction amount + incentive)
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        // Save updated balances
        userRepository.save(sender);
        userRepository.save(recipient);

        // Record the transaction with incentive
        TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
        transactionRecordRepository.save(record);

        System.out.println("Transaction processed: " + sender.getName() + " -> " +
                recipient.getName() + " : " + transaction.getAmount() + " (incentive: " + incentiveAmount + ")");
        return true;
    }
}