package com.example.bankapp.service;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final BankTransactionRepository transactionRepository;

    // Save failed transaction independently
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String reason) {

        BankTransaction failedTransaction = new BankTransaction(
                fromAccount,
                toAccount,
                amount,
                TransactionStatus.FAILED,
                reason);

        transactionRepository.save(failedTransaction);
    }
}