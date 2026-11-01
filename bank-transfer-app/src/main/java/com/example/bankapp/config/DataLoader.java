
package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadSampleData(CustomerRepository customerRepository) {

        return args -> {

            // Insert sample customers only if the database is empty
            if (customerRepository.count() == 0) {

                Customer customer1 = new Customer(
                        "100001",
                        "Kamal Perera",
                        "kamal@example.com",
                        new BigDecimal("50000.00"));

                Customer customer2 = new Customer(
                        "100002",
                        "Nimali Silva",
                        "nimali@example.com",
                        new BigDecimal("25000.00"));

                Customer customer3 = new Customer(
                        "100003",
                        "Saman Fernando",
                        "saman@example.com",
                        new BigDecimal("10000.00"));

                Customer bankAccount = new Customer(
                        "999999",
                        "Bank Fee Account",
                        "bankfees@example.com",
                        new BigDecimal("0.00"));

                customerRepository.saveAll(
                        List.of(customer1, customer2, customer3,bankAccount));

                System.out.println("Sample customers inserted successfully!");
            } else {
                System.out.println("Sample data already exists. Skipping insertion.");
            }
        };
    }
}
