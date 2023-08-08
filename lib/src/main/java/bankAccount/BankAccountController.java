package bankAccount;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import transaction.Transaction;

@Controller
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/bankaccounts")
    public String getUserBankAccount(Model model, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            double balance = bankAccountService.getBalanceForUser(loggedInUser);
            List<Transaction> transactions = bankAccountService.getTransactionsForUser(loggedInUser);
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("balance", balance);
            model.addAttribute("transactions", transactions);
            return "bankaccounts"; // Returning the view for the logged-in user
        } else {
            return "redirect:/login"; // Redirect to login if the user is not logged in
        }
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        try {
            if (bankAccountService.verifyLogin(username, password)) {
                session.setAttribute("loggedInUser", username);
                return "redirect:/bankaccounts";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/create-account")
    public String createBankAccount(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        try {
            bankAccountService.createBankAccount(username, password, 0.0);
            session.setAttribute("loggedInUser", username);
            return "redirect:/bankaccounts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/bankaccounts/deposit")
    public String deposit(@RequestParam String username, @RequestParam double amount, Model model, HttpSession session) {
        try {
            bankAccountService.deposit(username, amount);
            return "redirect:/bankaccounts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return getUserBankAccount(model, session);
        }
    }

    @PostMapping("/bankaccounts/withdraw")
    public String withdraw(@RequestParam String username, @RequestParam double amount, Model model, HttpSession session) {
        try {
            bankAccountService.withdraw(username, amount);
            return "redirect:/bankaccounts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return getUserBankAccount(model, session);
        }
    }
    
    @PostMapping("/bankaccounts/transfer")
    public String transfer(@RequestParam String sourceUsername, @RequestParam String targetUsername, @RequestParam double amount, Model model, HttpSession session) {
        try {
            bankAccountService.transfer(sourceUsername, targetUsername, amount);
            return "redirect:/bankaccounts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return getUserBankAccount(model, session);
        }
    }
}
