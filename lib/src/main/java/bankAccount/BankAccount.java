package bankAccount;

import exceptions.InsufficientBalanceException;
import transaction.ETransactionType;
import transaction.Transaction;
import database.DatabaseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankAccount {
    private static final Logger logger = LoggerFactory.getLogger(BankAccount.class);
    private static final double MAX_TRANSACTION_AMOUNT = 10000;

    private double balance;
    private final String username;
    private final Validator validator;

    public BankAccount(String username, double initialBalance) {
        this.balance = initialBalance;
        this.username = username;
        this.validator = new Validator();
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
    
    private class Validator {
    	private void validateTransaction(double amount, ETransactionType type) {
            validateAmount(amount);
            switch (type) {
                case DEPOSIT:
                case TRANSFER:
                    validateIncomingsLimit(amount);
                    break;
                case WITHDRAW:
                    validateSufficientBalance(amount);
                    validateOutgoingsLimit(amount);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid transaction type");
            }
        }
    	
    	private void validateTransfer(BankAccount destination, double amount) {
            validateAmount(amount);
            validateSufficientBalance(amount);
            validateOutgoingsLimit(amount);
            validateIncomingsLimitForDestinationAccount(destination.getUsername(), amount);
            if (username.equals(destination.getUsername())) {
                throw new IllegalArgumentException("Destination account can't be your current account");
            }
        }

        private void validateAmount(double amount) {
            if (amount <= 0 || amount > MAX_TRANSACTION_AMOUNT) {
                logger.error("Account {}. Attempted to deposit illegal amount: {}", username, amount);
                throw new IllegalArgumentException("Amount should be greater than 0 and less than or equal to " + MAX_TRANSACTION_AMOUNT);
            }
        }

        private void validateSufficientBalance(double amount) {
            if (amount > balance) {
                logger.error("Account {}. Attempted to withdraw more than current balance. Withdrawal amount: {}. Current balance: {}", username, amount, balance);
                throw new InsufficientBalanceException("Cannot withdraw more than current balance.");
            }
        }

        private void validateIncomingsLimit(double amount) {
            double totalIncomingsToday = DatabaseRepository.findTotalIncomingsTodayByUsername(username);
            double remainingLimit = MAX_TRANSACTION_AMOUNT - totalIncomingsToday;
            if (totalIncomingsToday + amount > MAX_TRANSACTION_AMOUNT) {
                String errorMessage = String.format("Transaction failed. The amount you are trying to receive exceeds the daily incoming limit. Remaining limit: %.2f€", remainingLimit);
                throw new IllegalArgumentException(errorMessage);
            }
        }

        private void validateIncomingsLimitForDestinationAccount(String username, double amount) {
            double totalIncomingsToday = DatabaseRepository.findTotalIncomingsTodayByUsername(username);
            double remainingLimit = MAX_TRANSACTION_AMOUNT - totalIncomingsToday;
            if (totalIncomingsToday + amount > MAX_TRANSACTION_AMOUNT) {
                String errorMessage = String.format("Transaction failed. The amount you are trying to send exceeds the destination's daily incoming limit. Remaining limit: %.2f€", remainingLimit);
                throw new IllegalArgumentException(errorMessage);
            }
        }

        private void validateOutgoingsLimit(double amount) {
            double totalOutgoingsToday = DatabaseRepository.findTotalOutgoingsTodayByUsername(username);
            double remainingLimit = MAX_TRANSACTION_AMOUNT - totalOutgoingsToday;
            if (totalOutgoingsToday + amount > MAX_TRANSACTION_AMOUNT) {
                String errorMessage = String.format("Transaction failed. The amount you are trying to send exceeds the daily outgoing limit. Remaining limit: %.2f€", remainingLimit);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
    
}
