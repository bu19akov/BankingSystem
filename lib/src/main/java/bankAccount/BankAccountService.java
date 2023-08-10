package bankAccount;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import database.DatabaseRepository;
import transaction.Transaction;

@Service
public class BankAccountService {

    private static final Logger logger = LoggerFactory.getLogger(BankAccount.class);

    public boolean verifyLogin(String username, String password) {
        return DatabaseRepository.verifyLogin(username, password);
    }

    public void createBankAccount(String username, String password, double initialBalance) {
        if (DatabaseRepository.findAccountByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        BankAccount account = new BankAccount(username, initialBalance);
        logger.info("A new bank account was created with username {}. Initial balance: {}", username, initialBalance);
        DatabaseRepository.saveAccount(account, password);
    }

    public double getBalanceForUser(String username) {
        BankAccount account = findAccountByUsername(username);
        return account.getBalance();
    }

    public List<Transaction> getTransactionsForUser(String username) {
        return DatabaseRepository.findAllTransactionsByUsername(username);
    }

    public void deposit(String username, double amount) {
        BankAccount account = findAccountByUsername(username);
        account.deposit(amount);
        DatabaseRepository.updateAccount(account);
    }

    public void withdraw(String username, double amount) {
        BankAccount account = findAccountByUsername(username);
        account.withdraw(amount);
        DatabaseRepository.updateAccount(account);
    }

    public void transfer(String sourceUsername, String targetUsername, double amount) {
        BankAccount sourceAccount = findAccountByUsername(sourceUsername);
        BankAccount targetAccount = findAccountByUsername(targetUsername);
        if (sourceAccount.getUsername().equals(targetAccount.getUsername())) {
            throw new IllegalArgumentException("Target account can't be your current account");
        }
        sourceAccount.transfer(targetAccount, amount);
        DatabaseRepository.updateAccount(sourceAccount);
        DatabaseRepository.updateAccount(targetAccount);
    }

    public void receiveForeignTransfer(String foreignUsername, String targetUsername, double amount) {
        if (foreignUsername == null || foreignUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username can't be empty");
        }
        BankAccount targetAccount = findAccountByUsername(targetUsername);
        targetAccount.receiveForeignTransfer(foreignUsername, amount);
        DatabaseRepository.updateAccount(targetAccount);
    }

    private BankAccount findAccountByUsername(String username) {
        BankAccount account = DatabaseRepository.findAccountByUsername(username);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return account;
    }
}
