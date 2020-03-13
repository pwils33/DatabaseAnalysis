package doas;

import models.Account;
import models.AccountId;

import java.util.List;

public interface AccountDAO {

    void createAccountStorage();

    void deleteAccountStorage();

    Account getAccount(AccountId accountId);

    List<Account> getAccounts(List<AccountId> accountIds);

    boolean insertAccount(Account account);

    boolean insertAccounts(List<Account> accounts);
}
