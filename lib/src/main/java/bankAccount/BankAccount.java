package bankAccount;

import transaction.ETransactionType;
import transaction.Transaction;
import database.DatabaseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankAccount {
    private static final Logger logger = LoggerFactory.getLogger(BankAccount.class);

    private double balance;
    private final String username;
    private final Validator validator;

    public BankAccount(String username, double initialBalance) {
        this.balance = initialBalance;
        this.username = username;
        this.validator = new Validator(username, initialBalance);
    }

    public synchronized void deposit(double amount) {
    	validator.validateTransaction(amount, ETransactionType.DEPOSIT);
        internalDeposit(amount);
        createTransaction(this.username, amount, ETransactionType.DEPOSIT);
    }

    public synchronized void withdraw(double amount) {
    	validator.validateTransaction(amount, ETransactionType.WITHDRAW);
        internalWithdraw(amount);
        createTransaction(this.username, amount, ETransactionType.WITHDRAW);
    }

    public void transfer(BankAccount destination, double amount) {
    	validator.validateTransfer(destination, amount);
    	
        BankAccount first = this.username.compareTo(destination.username) < 0 ? this : destination;
        BankAccount second = this.username.compareTo(destination.username) >= 0 ? this : destination;

        synchronized (first) {
            synchronized (second) {
                this.internalWithdraw(amount);
                destination.internalDeposit(amount);
                createTransaction(destination.username, amount, ETransactionType.TRANSFER);
                logger.info("Account {}. Money transferred. Amount: {}. New balance: {}. Destination account: {}", this.getUsername(), amount, this.balance, destination.getUsername());
            }
        }
    }
    
    public synchronized void receiveForeignTransfer(String foreignUsername, double amount) {
    	validator.validateTransaction(amount, ETransactionType.TRANSFER);
        this.internalDeposit(amount);
        createForeignTransaction(foreignUsername, amount, ETransactionType.TRANSFER);
        logger.info("Account {}. Transaction received from {}. Amount: {}. New balance: {}", this.getUsername(), foreignUsername, amount, this.balance);
    }
    
    private synchronized void internalDeposit(double amount) {
        this.balance += amount;
        logger.info("Account {}. Money deposited. Amount: {}. New balance: {}", this.getUsername(), amount, this.balance);
    }

    private synchronized void internalWithdraw(double amount) {
        this.balance -= amount;
        logger.info("Account {}. Money withdrawn. Amount: {}. New balance: {}", this.getUsername(), amount, this.balance);
    }
    
    public void createTransaction(String destinationAccountUsername, double amount, ETransactionType type) {
    	DatabaseRepository.saveTransaction(new Transaction(this.username, destinationAccountUsername, amount, type));
    }
    
    public void createForeignTransaction(String foreignUsername, double amount, ETransactionType type) {
    	DatabaseRepository.saveTransaction(new Transaction(foreignUsername, this.username, amount, type));
    }
    
    public synchronized double getBalance() {
        return this.balance;
    }
    
    public String getUsername() {
		return this.username;
    }
}
