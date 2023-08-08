package transaction;

import java.util.Date;
import java.util.UUID;

import java.text.SimpleDateFormat;

public class Transaction {
	
    private UUID id;
    private String sourceAccountUsername;
    private String destinationAccountUsername;
    private double amount;
    private ETransactionType type;
    private String timestamp;

    public Transaction(String sourceAccountUsername, String destinationAccountUsername, double amount, ETransactionType type) {
    	this.id = UUID.randomUUID();
    	this.sourceAccountUsername = sourceAccountUsername;
    	this.destinationAccountUsername = destinationAccountUsername;
    	this.amount = amount;
    	this.type = type;
    	this.timestamp = getCurrentTimestamp();
    }
    
    // for DatabaseRepository.findAllTransactionsByUsername()
    public Transaction(UUID id, String sourceAccountUsername, String destinationAccountUsername, double amount, ETransactionType type, String timestamp) {
    	this.id = id;
    	this.sourceAccountUsername = sourceAccountUsername;
    	this.destinationAccountUsername = destinationAccountUsername;
    	this.amount = amount;
    	this.type = type;
    	this.timestamp = timestamp;
    }

	public UUID getId() {
		return this.id;
	}

	public String getSourceAccountUsername() {
		return sourceAccountUsername;
	}

	public String getDestinationAccountUsername() {
		return destinationAccountUsername;
	}

	public double getAmount() {
		return amount;
	}
	
	public ETransactionType getType() {
		return type;
	}    
	
	public String getTimestamp() {
        return timestamp;
    }
	
	private String getCurrentTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return formatter.format(new Date());
    }
}