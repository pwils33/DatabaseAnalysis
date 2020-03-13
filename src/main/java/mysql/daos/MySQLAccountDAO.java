package mysql.daos;

import doas.AccountDAO;
import models.Account;
import models.AccountId;
import models.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLAccountDAO implements AccountDAO {

//    private ConnectionWrapper connectionWrapper;
    private MySQLUserDAO userDAO;

    public MySQLAccountDAO(MySQLUserDAO userDAO) {
//        this.connectionWrapper = connectionWrapper;
        this.userDAO = userDAO;
    }

    public void createAccountStorage() {
    }

    public void deleteAccountStorage() {
    }

    public Account getAccount(AccountId accountId) {
        return new Account(accountId, userDAO.getUsersByAccountId(accountId));
    }

    public List<Account> getAccounts(List<AccountId> accountIds) {
        List<Account> accounts = new ArrayList<>();
        for (AccountId accountId: accountIds) {
            accounts.add(getAccount(accountId));
        }
        return accounts;
    }

    public boolean insertAccount(Account account) {
        return userDAO.insertUsers(account.users);
    }

    public boolean insertAccounts(List<Account> accounts) {
        boolean allSuccess = true;
        for (Account account: accounts) {
            allSuccess = insertAccount(account) && allSuccess;
        }
        return allSuccess;
    }
}
