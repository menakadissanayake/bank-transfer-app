package com.example.bankapp.service;

import com.example.bankapp.model.Customer;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;

import com.example.bankapp.repository.CustomerRepository;
import com.example.bankapp.repository.BankTransactionRepository;

import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    // Search customers by name
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return customerRepository.findAllOrdered();
        }

        return customerRepository.searchByName(keyword);
    }

    // Get total balance of all customers
    public BigDecimal getTotalBalance() {
        return customerRepository.getTotalBalance();
    }

    // Get all latest transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    // Get transaction history of an account
    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    // Count successful transactions
    public long countSuccessfulTransactions() {
        return transactionRepository.countByStatus(TransactionStatus.SUCCESS);
    }

    // Count failed transactions
    public long countFailedTransactions() {
        return transactionRepository.countByStatus(TransactionStatus.FAILED);
    }

    // Create a new customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new BankException("Account number already exists");
        }

        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new BankException("Email already exists");
        }

        return customerRepository.save(customer);
    }

    // Transfer money between two accounts
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        // Step 1: Validate accounts
        if (fromAccount.equals(toAccount)) {
            throw new BankException("Accounts must be different");
        }

        Customer sender = customerRepository
                .getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        // Validate transfer amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Transfer amount must be greater than zero");
        }

        // Step 2: Debit money from sender
        int debited = customerRepository.debit(fromAccount, amount);

        if (debited == 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Step 3: Simulate a system failure
        if (simulateFailure) {
            throw new BankException("Simulated system failure. All changes were rolled back.");
        }

        // Step 4: Credit money to receiver
        int credited = customerRepository.credit(toAccount, amount);

        if (credited == 0) {
            throw new AccountNotFoundException("Receiver account not found");
        }

        // Step 5: Save successful transaction
        BankTransaction transaction = new BankTransaction(
                fromAccount,
                toAccount,
                amount,
                TransactionStatus.SUCCESS,
                "Transfer completed successfully");

        return transactionRepository.save(transaction);
    }
}