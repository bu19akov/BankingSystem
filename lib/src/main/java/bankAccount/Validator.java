package bankAccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.DatabaseRepository;
import exceptions.InsufficientBalanceException;
import transaction.ETransactionType;

public class Validator {
    private static final Logger logger = LoggerFactory.getLogger(Validator.class);
    private static final double MAX_TRANSACTION_AMOUNT = 10000;
    private final String username;
    private final double balance;

    public Validator(String username, double balance) {
        this.username = username;
        this.balance = balance;
    }

    public void validateTransaction(double amount, ETransactionType type) {
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
	
    public void validateTransfer(BankAccount destination, double amount) {
        validateAmount(amount);
        validateSufficientBalance(amount);
        validateOutgoingsLimit(amount);
        validateIncomingsLimitForDestinationAccount(destination.getUsername(), amount);
        if (username.equals(destination.getUsername())) {
            throw new IllegalArgumentException("Destination account can't be your current account");
        }
    }

    public void validateAmount(double amount) {
        if (amount <= 0 || amount > MAX_TRANSACTION_AMOUNT) {
            logger.error("Account {}. Attempted to deposit illegal amount: {}", username, amount);
            throw new IllegalArgumentException("Amount should be greater than 0 and less than or equal to " + MAX_TRANSACTION_AMOUNT);
        }
    }

    public void validateSufficientBalance(double amount) {
        if (amount > balance) {
            logger.error("Account {}. Attempted to withdraw more than current balance. Withdrawal amount: {}. Current balance: {}", username, amount, balance);
            throw new InsufficientBalanceException("Cannot withdraw more than current balance.");
        }
    }

    public void validateIncomingsLimit(double amount) {
        double totalIncomingsToday = DatabaseRepository.findTotalIncomingsTodayByUsername(username);
        double remainingLimit = MAX_TRANSACTION_AMOUNT - totalIncomingsToday;
        if (totalIncomingsToday + amount > MAX_TRANSACTION_AMOUNT) {
            String errorMessage = String.format("Transaction failed. The amount you are trying to receive exceeds the daily incoming limit. Remaining limit: %.2f€", remainingLimit);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public void validateIncomingsLimitForDestinationAccount(String username, double amount) {
        double totalIncomingsToday = DatabaseRepository.findTotalIncomingsTodayByUsername(username);
        double remainingLimit = MAX_TRANSACTION_AMOUNT - totalIncomingsToday;
        if (totalIncomingsToday + amount > MAX_TRANSACTION_AMOUNT) {
            String errorMessage = String.format("Transaction failed. The amount you are trying to send exceeds the destination's daily incoming limit. Remaining limit: %.2f€", remainingLimit);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public void validateOutgoingsLimit(double amount) {
        double totalOutgoingsToday = DatabaseRepository.findTotalOutgoingsTodayByUsername(username);
        double remainingLimit = MAX_TRANSACTION_AMOUNT - totalOutgoingsToday;
        if (totalOutgoingsToday + amount > MAX_TRANSACTION_AMOUNT) {
            String errorMessage = String.format("Transaction failed. The amount you are trying to send exceeds the daily outgoing limit. Remaining limit: %.2f€", remainingLimit);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
