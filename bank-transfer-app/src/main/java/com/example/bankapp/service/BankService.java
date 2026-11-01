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
import java.math.RoundingMode;
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

    // Get large transfers
    public List<BankTransaction> getLargeTransfers(BigDecimal minAmount) {
        return transactionRepository.findLargeTransfers(minAmount);
    }

    // Get total successful amount sent by an account
    public BigDecimal getTotalSentByAccount(String accountNumber) {
        return transactionRepository.getTotalSentByAccount(accountNumber);
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

    // Transfer money between two accounts with 1% fee
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        // Step 1: Validate input
        if (fromAccount == null || toAccount == null
                || fromAccount.isBlank() || toAccount.isBlank()) {
            throw new BankException("Account numbers are required");
        }

        if (fromAccount.equals(toAccount)) {
            throw new BankException("Accounts must be different");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Transfer amount must be greater than zero");
        }

        // Step 2: Check sender and receiver accounts
        customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        // Step 3: Calculate 1% fee
        BigDecimal fee = amount.multiply(new BigDecimal("0.01"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDebit = amount.add(fee);

        // Step 4: Debit sender (amount + fee)
        int debited = customerRepository.debit(fromAccount, totalDebit);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance to cover transfer amount and fee");
        }

        // Step 5: Credit receiver (transfer amount)
        int receiverCredited = customerRepository.credit(toAccount, amount);

        if (receiverCredited == 0) {
            throw new AccountNotFoundException("Receiver account not found");
        }

        // Step 6: Credit bank fee account (999999)
        int bankCredited = customerRepository.credit("999999", fee);

        if (bankCredited == 0) {
            throw new AccountNotFoundException(
                    "Bank fee account 999999 not found");
        }

        // Step 7: Simulate failure after all balance changes
        if (simulateFailure) {
            throw new BankException(
                    "Simulated system failure. All changes were rolled back.");
        }

        // Step 8: Save successful transaction
        BankTransaction transaction = new BankTransaction(
                fromAccount,
                toAccount,
                amount,
                TransactionStatus.SUCCESS,
                "Transfer completed successfully. Fee: LKR " + fee);

        return transactionRepository.save(transaction);
    }

    // Deposit money into an account
    @Transactional
    public BankTransaction deposit(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Deposit amount must be greater than zero");
        }

        customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        int updated = customerRepository.credit(accountNumber, amount);

        if (updated == 0) {
            throw new AccountNotFoundException("Account not found");
        }

        BankTransaction transaction = new BankTransaction(
                "CASH",
                accountNumber,
                amount,
                TransactionStatus.SUCCESS,
                "Cash deposit");

        return transactionRepository.save(transaction);
    }

    // Withdraw money from an account
    @Transactional
    public BankTransaction withdraw(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Withdrawal amount must be greater than zero");
        }

        customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        int updated = customerRepository.debit(accountNumber, amount);

        if (updated == 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        BankTransaction transaction = new BankTransaction(
                accountNumber,
                "CASH",
                amount,
                TransactionStatus.SUCCESS,
                "Cash withdrawal");

        return transactionRepository.save(transaction);
    }

}