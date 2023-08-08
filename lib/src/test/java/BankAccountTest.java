import exceptions.InsufficientBalanceException;
import exceptions.NegativeAmountException;

import testResult.TestResultLogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import bankAccount.BankAccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ExtendWith(TestResultLogger.class)
public class BankAccountTest {
    private BankAccount account;
    private BankAccount destinationAccount;

    @BeforeEach
    public void setUp() {
        account = new BankAccount("test", 1000.0);
        destinationAccount = new BankAccount("testDest", 2000.0);
    }

    @Test
    public void deposit500_DepositMethod_ShouldIncreaseBalanceBy500() {
        account.deposit(500.0);
        assertEquals(1500.0, account.getBalance(), 0.0001);
    }

    @Test
    public void depositNegative500_DepositMethod_ShouldThrowNegativeAmountException() {
        assertThrows(NegativeAmountException.class, () -> {
            account.deposit(-500.0);
        });
    }

    @Test
    public void withdraw500_WithdrawMethod_ShouldDecreaseBalanceBy500() {
        account.withdraw(500.0);
        assertEquals(500.0, account.getBalance(), 0.0001);
    }

    @Test
    public void withdrawNegative500_WithdrawMethod_ShouldThrowNegativeAmountException() {
        assertThrows(NegativeAmountException.class, () -> {
            account.withdraw(-500.0);
        });
    }

    @Test
    public void withdraw1500_WithdrawMethod_ShouldThrowInsufficientBalanceException() {
        assertThrows(InsufficientBalanceException.class, () -> {
            account.withdraw(1500.0);
        });
    }

    @Test
    public void transfer500_TransferMethod_ShouldDecreaseBalanceBy500AndIncreaseDestinationBalanceBy500() {
        account.transfer(destinationAccount, 500.0);
        assertEquals(500.0, account.getBalance(), 0.0001);
        assertEquals(2500.0, destinationAccount.getBalance(), 0.0001);
    }

    @Test
    public void transferNegative500_TransferMethod_ShouldThrowNegativeAmountException() {
        assertThrows(NegativeAmountException.class, () -> {
            account.transfer(destinationAccount, -500.0);
        });
    }

    @Test
    public void transfer1500_TransferMethod_ShouldThrowInsufficientBalanceException() {
        assertThrows(InsufficientBalanceException.class, () -> {
            account.transfer(destinationAccount, 1500.0);
        });
    }

    @Test
    public void checkBalance_GetBalanceMethod_ShouldReturn1000() {
        assertEquals(1000.0, account.getBalance(), 0.0001);
    }
    
    @Test
    public void transferMoney_MultipleThreads_ShouldNotDeadlock() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(2);
        AtomicBoolean failed = new AtomicBoolean(false);

        Runnable transferAtoB = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                	account.transfer(destinationAccount, 1.0);
                } catch (Exception e) {
                    failed.set(true);
                }
            }
        };

        Runnable transferBtoA = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                	destinationAccount.transfer(account, 1.0);
                } catch (Exception e) {
                    failed.set(true);
                }
            }
        };

        service.submit(transferAtoB);
        service.submit(transferBtoA);

        service.shutdown();
        try {
            // Wait for 30 seconds for threads to finish, if not finished then assume a deadlock and fail the test.
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                throw new TimeoutException();
            }
        } catch (Exception e) {
            failed.set(true);
        }

        assertFalse(failed.get());
    }
}
