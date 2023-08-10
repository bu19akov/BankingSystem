package bankAccount;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Controller
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/bankaccounts")
    public String getUserBankAccount(Model model, HttpSession session) {
    	if (!isLoggedIn(session)) return "redirect:/login";
    	String loggedInUser = getLoggedInUser(session);
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("balance", bankAccountService.getBalanceForUser(loggedInUser));
        model.addAttribute("transactions", bankAccountService.getTransactionsForUser(loggedInUser));
        return "bankaccounts"; // Returning the view for the logged-in user
    }
    
    @GetMapping("/bankaccounts/charity")
    public String charityPage(Model model, HttpSession session) {
    	if (!isLoggedIn(session)) return "redirect:/login";
    	String loggedInUser = getLoggedInUser(session);
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("balance", bankAccountService.getBalanceForUser(loggedInUser));
        getBalanceForCharities(model);
        return "charity"; // Returning the view for the logged-in user
    }
    
    private boolean isLoggedIn(HttpSession session) {
        return getLoggedInUser(session) != null;
    }
    
    private String getLoggedInUser(HttpSession session) {
        return (String) session.getAttribute("loggedInUser");
    }
    
    private void getBalanceForCharities(Model model) {
        model.addAttribute("balanceSaveTheForests", bankAccountService.getBalanceForUser("SaveTheForests"));
        model.addAttribute("balanceFeedTheChildren", bankAccountService.getBalanceForUser("FeedTheChildren"));
        model.addAttribute("balanceProtectTheAnimals", bankAccountService.getBalanceForUser("ProtectTheAnimals"));
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
    public String transfer(@RequestParam String sourceUsername, @RequestParam String targetUsername, @RequestParam double amount, @RequestParam String redirectUrl, Model model, HttpSession session) {
        try {
            bankAccountService.transfer(sourceUsername, targetUsername, amount);
            return "redirect:" + redirectUrl; // Redirect to the specified URL
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            if (redirectUrl.equals("/bankaccounts")) {
            	return getUserBankAccount(model, session);
            } else {
            	return charityPage(model, session);
            }
        }
    }
    
    @PostMapping("/bankaccounts/api/transfer")
    public ResponseEntity<Object> receiveForeignTransfer(@RequestParam String sourceAccountUsername, @RequestParam String destinationAccountUsername, @RequestParam double amount) {
        try {
            bankAccountService.receiveForeignTransfer(sourceAccountUsername, destinationAccountUsername, amount);
            return new ResponseEntity<>("Transaction was successfull", HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception if needed
            return new ResponseEntity<>("Transaction failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
