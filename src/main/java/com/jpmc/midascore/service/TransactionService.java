package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionService(UserRepository userRepository,
                              TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
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

        // Process the transaction
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());

        // Save updated balances
        userRepository.save(sender);
        userRepository.save(recipient);

        // Record the transaction
        TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount());
        transactionRecordRepository.save(record);

        System.out.println("Transaction processed: " + sender.getName() + " -> " +
                recipient.getName() + " : " + transaction.getAmount());
        return true;
    }
}