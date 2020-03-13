package redis;

import doas.AccountDAO;
import models.Account;
import models.AccountId;

import java.util.ArrayList;
import java.util.List;

public class RedisAccountDAO implements AccountDAO {

    private RedisUserDAO userDAO;

    public RedisAccountDAO(RedisUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void createAccountStorage() {}

    public void deleteAccountStorage() {}

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
        for (Account account: accounts) {
            insertAccount(account);
        }
        return true;
    }
}
