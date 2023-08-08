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
        BankAccount account = DatabaseRepository.findAccountByUsername(username);
        if (account != null) {
            return account.getBalance();
        } else {
            throw new IllegalArgumentException("Account not found");
        }
    }
    
    public List<Transaction> getTransactionsForUser(String username) {
        return DatabaseRepository.findAllTransactionsByUsername(username);
    }

    public void deposit(String username, double amount) {
        BankAccount account = DatabaseRepository.findAccountByUsername(username);
        if (account != null) {
            account.deposit(amount);
            DatabaseRepository.updateAccount(account);
        } else {
            throw new IllegalArgumentException("Account not found");
        }
    }

    public void withdraw(String username, double amount) {
        BankAccount account = DatabaseRepository.findAccountByUsername(username);
        if (account != null) {
            account.withdraw(amount);
            DatabaseRepository.updateAccount(account);
        } else {
            throw new IllegalArgumentException("Account not found");
        }
    }
    
    public void transfer(String sourceUsername, String targetUsername, double amount) {
        BankAccount sourceAccount = DatabaseRepository.findAccountByUsername(sourceUsername);
        BankAccount targetAccount = DatabaseRepository.findAccountByUsername(targetUsername);
        if (sourceAccount != null && targetAccount != null) {
        	if (sourceAccount.getUsername().equals(targetAccount.getUsername())) {
            	throw new IllegalArgumentException("Target account can't be your current account");
            }
            sourceAccount.transfer(targetAccount, amount);
            DatabaseRepository.updateAccount(sourceAccount);
            DatabaseRepository.updateAccount(targetAccount);
        } else {
            throw new IllegalArgumentException("Target account not found");
        }
    }
}
