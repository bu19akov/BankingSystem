package bankAccount;

import exceptions.InsufficientBalanceException;
import exceptions.NegativeAmountException;
import transaction.ETransactionType;
import transaction.Transaction;
import database.DatabaseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankAccount {
    private static final Logger logger = LoggerFactory.getLogger(BankAccount.class);

    private double balance;
    private final String username;

    public BankAccount(String username, double initialBalance) {
        this.balance = initialBalance;
        this.username = username;
    }

    private synchronized void internalDeposit(double amount) {
        handleNegativeAmount(amount);
        this.balance += amount;
        logger.info("Account {}. Money deposited. Amount: {}. New balance: {}", this.getUsername(), amount, this.balance);
    }

    private synchronized void internalWithdraw(double amount) {
        handleNegativeAmount(amount);
        handleInsufficientBalance(amount);
        this.balance -= amount;
        logger.info("Account {}. Money withdrawn. Amount: {}. New balance: {}", this.getUsername(), amount, this.balance);
    }

    public synchronized void deposit(double amount) {
        internalDeposit(amount);
        createTransaction(this.username, amount, ETransactionType.DEPOSIT);
    }

    public synchronized void withdraw(double amount) {
        internalWithdraw(amount);
        createTransaction(this.username, amount, ETransactionType.WITHDRAW);
    }

    public void transfer(BankAccount destination, double amount) {
    	// lock the account with the lower id first to prevent the possibility of a deadlock
    	// two-step locking
        BankAccount first = this.username.compareTo(destination.username) < 0 ? this : destination;
        BankAccount second = this.username.compareTo(destination.username) >= 0 ? this : destination;

        synchronized (first) {
            synchronized (second) {
                handleNegativeAmount(amount);
                handleInsufficientBalance(amount);
                this.internalWithdraw(amount);
                destination.internalDeposit(amount);
                createTransaction(destination.username, amount, ETransactionType.TRANSFER);
                logger.info("Account {}. Money transferred. Amount: {}. New balance: {}. Destination account: {}", this.getUsername(), amount, this.balance, destination.getUsername());
            }
        }
    }
    
    public void handleNegativeAmount(double amount) {
    	if (amount < 0) {
    		logger.error("Account {}. Attempted to deposit negative amount: {}", this.getUsername(), amount);
    		throw new NegativeAmountException("Cannot deposit negative amount.");
    	}
    }
    
    public void handleInsufficientBalance(double amount) {
    	if (amount > this.balance) {
    		logger.error("Account {}. Attempted to withdraw more than current balance. Withdrawal amount: {}. Current balance: {}", this.getUsername(), amount, this.balance);
    		throw new InsufficientBalanceException("Cannot withdraw more than current balance.");
    	}
    }
    
    public void createTransaction(String destinationAccountUsername, double amount, ETransactionType type) {
    	Transaction transaction = new Transaction(this.username, destinationAccountUsername, amount, type);
    	DatabaseRepository.saveTransaction(transaction);
    }
    
    public synchronized double getBalance() {
        return this.balance;
    }
    
    public String getUsername() {
		return this.username;
    }
}
