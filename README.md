# Banking System Project

This project is a web-based banking system that allows users to manage bank accounts, perform transactions, and more. It includes features such as deposits, withdrawals, transfers, and supports charity accounts.

## Features

- **User Authentication**: Secure login functionality with account creation options.
- **Bank Account Management**: View account balance and transaction history.
- **Deposits and Withdrawals**: Perform deposits and withdrawals from user accounts.
- **Transfers**: Transfer money between user accounts and charities.
- **Charity Page**: View charity accounts' balances.
- **API Support**: Receive foreign transfers through an API endpoint.
- **Concurrency Support**: Synchronized methods for handling multiple transactions at once.
- **Transaction Logging**: Logger to track transaction details.

## Technologies Used

- **Spring Framework**: Used for dependency injection, controller definitions, and mapping HTTP requests.
- **SLF4J**: Logging framework for monitoring application activity.
- **Custom Validator**: Ensures transaction and transfer validity.

## Endpoints

- `/login`: Authentication and login page.
- `/logout`: Session invalidation and redirection to login.
- `/create-account`: Endpoint for creating a new bank account.
- `/bankaccounts`: Overview of user's bank accounts.
- `/bankaccounts/charity`: Overview of charity balances.
- `/bankaccounts/deposit`: Deposit endpoint.
- `/bankaccounts/withdraw`: Withdrawal endpoint.
- `/bankaccounts/transfer`: Transfer endpoint.
- `/bankaccounts/api/transfer`: API endpoint for foreign transfers.

## Installation & Setup

1. **Clone the repository**: Clone this repository to your local machine.
2. **Configure the Database**: Set up the database connection in `DatabaseRepository`.
3. **Build the Project**: Use Gradle to build the project.
4. **Run the Application**: Start the application using the main method or deploy it on a server like Tomcat.
5. **Access the Application**: Open a web browser and navigate to `http://localhost:port/login`.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Special thanks to the Spring community for the robust framework.

## Support

If you encounter any issues, please open an issue or contact the maintainers directly.

---

Made with :heart: by [bu19akov](https://www.linkedin.com/in/vbulgakov/). For more information, please visit the [project website](http://www.banking-system.online/login).
