package cassandra;

import doas.AccountDAO;
import models.Account;
import models.AccountId;

import java.util.ArrayList;
import java.util.List;

public class CassandraAccountDAO implements AccountDAO {

    private CassandraUserDAO userDAO;

    public CassandraAccountDAO(CassandraUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void createAccountStorage() {}

    public void deleteAccountStorage() {}

    public Account getAccount(AccountId accountId) {
        return new Account(accountId, this.userDAO.getUsersByAccountId(accountId));
    }

    public List<Account> getAccounts(List<AccountId> accountIds) {
        List<Account> accounts = new ArrayList<>();
        for (AccountId accountId: accountIds) {
            accounts.add(getAccount(accountId));
        }
        return accounts;
    }

    public boolean insertAccount(Account account) {
        userDAO.insertUsersByAccountId(account.accountId, account.users);
        return true;
    }

    public boolean insertAccounts(List<Account> accounts) {
        userDAO.insertUsersByBatch(accounts);
        return true;
    }
}
