package org.sid.ebankingbackend;

import org.sid.ebankingbackend.entities.*;
import org.sid.ebankingbackend.enums.AccountStatus;
import org.sid.ebankingbackend.enums.OperationType;
import org.sid.ebankingbackend.exception.BalanceNotSufficientException;
import org.sid.ebankingbackend.exception.BankAccountNotFoundException;
import org.sid.ebankingbackend.exception.CustomerNotFoundException;
import org.sid.ebankingbackend.repositories.AccountOperationRepository;
import org.sid.ebankingbackend.repositories.BankAccountRepository;
import org.sid.ebankingbackend.repositories.CustomerRepositoriy;
import org.sid.ebankingbackend.services.BankAccountService;
import org.sid.ebankingbackend.services.BankService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingBackendApplication.class, args);
    }

    //@Bean
    CommandLineRunner start(CustomerRepositoriy customerRepositoriy, BankAccountRepository bankAccountRepository, AccountOperationRepository accountOperationRepository) {
        return args -> {
            Stream.of("Hassan", "Yassin", "Aicha").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customerRepositoriy.save(customer);
            });
            customerRepositoriy.findAll().forEach(customer -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random() * 90000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(customer);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random() * 90000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(customer);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
            });
            bankAccountRepository.findAll().forEach(acc -> {
                for (int i = 0; i < 10; i++) {
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 12000);
                    accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
                    accountOperation.setBankAccount(acc);
                    accountOperationRepository.save(accountOperation);
                }

            });
        };
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService) {
        return args -> {

            Stream.of("Hassan", "Imane", "Mohamed").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                bankAccountService.saveCustomer(customer);
            });
bankAccountService.listCustomers().forEach(cust->{
    try {
        bankAccountService.saveCurrentBankAccount(Math.random()*90000,9000,cust.getId());
        bankAccountService.saveSavingBankAccount(Math.random()*120000,5.5,cust.getId());
        List<BankAccount> bankAccounts = bankAccountService.bankAccountList();
        for (BankAccount bankAccount:bankAccounts){
            for (int i=0;i<10;i++){
                bankAccountService.credit(bankAccount.getId(),10000+Math.random()*120000,"Credit");
                bankAccountService.debit(bankAccount.getId(),1000+Math.random()*9000,"Debit");
            }
        }
    } catch (CustomerNotFoundException e) {
        e.printStackTrace();
    } catch (BankAccountNotFoundException | BalanceNotSufficientException e) {
        throw new RuntimeException(e);
    }
});

        };


    }
}

