package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Display dashboard
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("customers",
                bankService.searchCustomers(keyword));

        model.addAttribute("totalBalance",
                bankService.getTotalBalance());

        model.addAttribute("transactions",
                bankService.getLatestTransactions());

        model.addAttribute("successCount",
                bankService.countSuccessfulTransactions());

        model.addAttribute("failedCount",
                bankService.countFailedTransactions());

        model.addAttribute("keyword", keyword);

        model.addAttribute("transferRequest", new TransferRequest());

        model.addAttribute("customer", new Customer());

        return "bank";
    }

    // Add a new customer
    @PostMapping("/customers")
    public String addCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.createCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Customer added successfully");

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/";
    }

    // Transfer money between accounts
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest") TransferRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Check form validation errors
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    bindingResult.getFieldError().getDefaultMessage());

            return "redirect:/";
        }

        try {
            // Call BankService to perform transfer
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transfer completed successfully!");

        } catch (RuntimeException e) {

            // This catch executes after the transfer transaction rolls back.
            // Save the failed transaction independently.
            auditService.recordFailure(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    e.getMessage());

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/";
    }

    // Display account transaction history
    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);

        model.addAttribute("transactions",
                bankService.getAccountHistory(accountNumber));

        model.addAttribute("totalSent",
                bankService.getTotalSentByAccount(accountNumber));

        return "history";
    }

    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000") BigDecimal min,
            Model model) {

        model.addAttribute("minAmount", min);
        model.addAttribute("transactions",
                bankService.getLargeTransfers(min));

        return "large-transfers";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Deposit completed successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/";
    }
@PostMapping("/withdraw")
public String withdraw(
        @RequestParam String accountNumber,
        @RequestParam BigDecimal amount,
        RedirectAttributes redirectAttributes) {

    try {
        bankService.withdraw(accountNumber, amount);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Withdrawal completed successfully");
    } catch (RuntimeException e) {
        redirectAttributes.addFlashAttribute(
                "errorMessage", e.getMessage());
    }

    return "redirect:/";
}
}