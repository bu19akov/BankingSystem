package database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import bankAccount.BankAccount;
import transaction.ETransactionType;
import transaction.Transaction;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import java.text.SimpleDateFormat;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRepository.class);
    
    private static final MongoClient mongoClient;
    private static final MongoDatabase database;
    private static final MongoCollection<Document> transactionsCollection;
    private static final MongoCollection<Document> accountsCollection;

    static {
        String connectionString = "mongodb+srv://vovabulgakov00:4EedWGgSPKpCrfGD@accenturebanktask.s2lp5bl.mongodb.net/?retryWrites=true&w=majority";
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("BankAccount");
        transactionsCollection = database.getCollection("Transactions");
        accountsCollection = database.getCollection("Accounts");
    }
    
    public static boolean verifyLogin(String username, String password) {
        Document document = new Document("username", username).append("password", password);
        return accountsCollection.find(document).first() != null;
    }
    
    public static BankAccount findAccountByUsername(String username) {
        Document query = new Document("username", username);
        Document document = accountsCollection.find(query).first();
        if (document != null) {
            String usernameFound = document.getString("username");
            double balance = document.getDouble("balance");
            return new BankAccount(usernameFound, balance);
        }
        return null;
    }
    
    public static double findTotalIncomingsTodayByUsername(String username) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dateToday = formatter.format(new Date());

        List<String> excludeDestinations = Arrays.asList("SaveTheForests", "FeedTheChildren", "ProtectTheAnimals");

        Document query = new Document("$and",
            Arrays.asList(
                new Document("destination_id", new Document("$nin", excludeDestinations)),
                new Document("destination_id", username),
                new Document("$or", Arrays.asList(
                    new Document("type", ETransactionType.DEPOSIT.toString()),
                    new Document("type", ETransactionType.TRANSFER.toString())
                )),
                new Document("timestamp", new Document("$regex", "^" + dateToday))
            )
        );

        double totalDepositsToday = 0;
        for (Document document : transactionsCollection.find(query)) {
            totalDepositsToday += document.getDouble("amount");
        }

        return totalDepositsToday;
    }

    
    public static double findTotalOutgoingsTodayByUsername(String username) {
    	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dateToday = formatter.format(new Date());

        Document query = new Document("$and",
    	    Arrays.asList(
    	        new Document("source_id", username),
    	        new Document("$or", Arrays.asList(
    	            new Document("type", ETransactionType.WITHDRAW.toString()),
    	            new Document("type", ETransactionType.TRANSFER.toString())
    	        )),
    	        new Document("timestamp", new Document("$regex", "^" + dateToday))
    	    )
    	);

        double totalDepositsToday = 0;
        for (Document document : transactionsCollection.find(query)) {
            totalDepositsToday += document.getDouble("amount");
        }

        return totalDepositsToday;
    }
    
    public static void saveAccount(BankAccount account, String password) {
        Document document = new Document("username", account.getUsername())
                .append("password", password)
                .append("balance", account.getBalance());
                
        try {
            accountsCollection.insertOne(document);
            logger.info("Account for {} saved successfully", account.getUsername());
        } catch (MongoException e) {
            logger.error("Failed to save account for {}", account.getUsername(), e);
        }
    }
    
    public static void updateAccount(BankAccount account) {
        Document filter = new Document("username", account.getUsername());
        Document update = new Document("$set", new Document("balance", account.getBalance()));
        
        try {
            accountsCollection.updateOne(filter, update);
            logger.info("Account for {} updated successfully", account.getUsername());
        } catch (MongoException e) {
            logger.error("Failed to update account for {}", account.getUsername(), e);
        }
    }

    public static List<Transaction> findAllTransactionsByUsername(String username) {
        List<Transaction> transactions = new ArrayList<>();
        Document query = new Document("$or", Arrays.asList(new Document("source_id", username), new Document("destination_id", username)));
        transactionsCollection.find(query).forEach(document -> {
            UUID id = UUID.fromString(document.getString("_id"));
            String sourceId = document.getString("source_id");
            String destinationId = document.getString("destination_id");
            double amount = document.getDouble("amount");
            String type = document.getString("type");
            String timestamp = document.getString("timestamp");
            transactions.add(new Transaction(id, sourceId, destinationId, amount, ETransactionType.valueOf(type), timestamp));
        });
        return transactions;
    }

    public static void saveTransaction(Transaction transaction) {
        Document document = new Document("_id", transaction.getId().toString())
        		.append("source_id", transaction.getSourceAccountUsername())
                .append("destination_id", transaction.getDestinationAccountUsername())
                .append("amount", transaction.getAmount())
                .append("type", transaction.getType().toString())
                .append("timestamp", transaction.getTimestamp());

        try {
        	transactionsCollection.insertOne(document);
            logger.info("Transaction {} saved successfully", transaction.getId());
        } catch (MongoException e) {
            logger.error("Failed to save transaction {}", transaction.getId(), e);
        }
    }
    
    public static void deleteAllTransactions() {
        try {
        	transactionsCollection.deleteMany(new Document()); // empty Document to match all
            logger.info("All transactions deleted successfully");
        } catch (MongoException e) {
            logger.error("Failed to delete all transactions", e);
        }
    }

    public static void close() {
        mongoClient.close();
        logger.info("Connection to Mongo is closed");
    }
}
