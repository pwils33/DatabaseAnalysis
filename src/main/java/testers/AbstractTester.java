package testers;

import doas.AccountDAO;
import doas.DocumentDAO;
import doas.UserDAO;
import models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class AbstractTester {

    protected abstract AccountDAO getAccountDAO();

    protected abstract UserDAO getUserDAO();

    protected abstract DocumentDAO getDocumentDAO();

    private void deleteAllStorage() {
        getAccountDAO().deleteAccountStorage();
        getUserDAO().deleteUserStorage();
        getDocumentDAO().deleteDocumentStorage();
    }

    private void setupStorage() {
        getAccountDAO().createAccountStorage();
        getUserDAO().createUserStorage();
        getDocumentDAO().createDocumentStorage();
    }

    public void testDatabase() {
        runTestForXAccounts(1);
        runTestForXAccounts(10);
    }

    private void runTestForXAccounts(int x) {
        deleteAllStorage();
        setupStorage();
        testAccounts(generateAccounts(x));
        deleteAllStorage();
    }

    private List<Account> generateAccounts(int generateAmount) {
        List<Account> accounts = new ArrayList<>(generateAmount);
        for (int i = 0; i < generateAmount; i++) {
            accounts.add(generateAccount());
        }
        return accounts;
    }

    private void testAccounts(List<Account> accounts) {
        AccountDAO accountDAO = getAccountDAO();
        UserDAO userDAO = getUserDAO();
        DocumentDAO documentDAO = getDocumentDAO();

        //Test account queries
        timeMethod(() -> accountDAO.insertAccounts(accounts), "insert " + accounts.size() + " accounts");
        List<AccountId> accountIds = new ArrayList<>(accounts.size());
        for (Account account: accounts) {
            accountIds.add(account.accountId);
        }
        timeMethod(() -> accountDAO.getAccounts(accountIds), "query for " + accountIds.size() + " accounts");
        Account randAccount = accounts.get(rand.nextInt(accounts.size()));
        timeMethod(() -> accountDAO.getAccount(randAccount.accountId), "query for random account");

        //Test user queries
        timeMethod(() -> userDAO.getUsersByAccountIds(accountIds), "query for users with " + accountIds.size() + " accountIds");
        timeMethod(() -> userDAO.getUsersByAccountId(randAccount.accountId), "query for users for single account");
        User randUser = randAccount.users.get(rand.nextInt(randAccount.users.size()));
        timeMethod(() -> userDAO.getUserByUserId(randUser.userId), "query for single user by userId");
        List<UserId> userIds = new ArrayList<>(randAccount.users.size());
        for (User user: randAccount.users) {
            userIds.add(user.userId);
        }
        timeMethod(() -> userDAO.getUsersByUserIds(userIds), "query for " + userIds.size() + " users by userIds");

        //Test document queries
        timeMethod(() -> documentDAO.getDocumentsByUserId(randUser.userId), "query for documents by userId");
        List<DocumentId> documentIds = new ArrayList<>(randUser.documents.size());
        for (Document document: randUser.documents) {
            documentIds.add(document.docId);
        }
        timeMethod(() -> documentDAO.getDocumentsByDocumentIds(documentIds), "query for documents by " + documentIds.size() + " docIds");
        if (randUser.documents.size() > 0) {
            Document randDoc = randUser.documents.get(rand.nextInt(randUser.documents.size()));
            timeMethod(() -> documentDAO.getDocumentByDocumentId(randDoc.docId), "query for document by single documentId");
        }
    }

    private interface quickMethod {
        void method();
    }

    private void timeMethod(quickMethod method, String signature) {
        long start = System.currentTimeMillis();
        method.method();
        long end = System.currentTimeMillis();
        System.out.println("testing " + signature + " took " + (end - start) + " milliseconds");
    }

    private String AlphaNumericString =
            "0123456789" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuwvxyz";

    private Random rand = new Random();

    private String randomString(int lengthLimit) {
        int length = rand.nextInt(lengthLimit);
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(AlphaNumericString.length());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    private Account generateAccount() {
        AccountId accountId = new AccountId(UUID.randomUUID());
        int numUsers = rand.nextInt(4) + 1;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < numUsers; i++) {
            users.add(generateUser(accountId));
        }
        return new Account(accountId, users);
    }

    private User generateUser(AccountId accountId) {
        int numDocs = rand.nextInt(8);
        List<Document> docs = new ArrayList<>();
        for (int i = 0; i < numDocs; i++) {
            docs.add(generateDocument());
        }
        return new User(randomString(6), new UserId(UUID.randomUUID()), accountId, docs);
    }

    private Document generateDocument() {
        int numPages = rand.nextInt(10);
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            pages.add(randomString(50));
        }
        return new Document(new DocumentId(UUID.randomUUID()), pages);
    }

}
