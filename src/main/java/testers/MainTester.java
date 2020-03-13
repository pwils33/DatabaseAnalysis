package testers;

public class MainTester {
    public static void main(String[] args) {
        AbstractTester tester = new MySQLTester();
        System.out.println("Testing MySQL");
        tester.testDatabase();
        tester = new MongoTester();
        System.out.println("Testing Mongo");
        tester.testDatabase();
        tester = new RedisTester();
        System.out.println("Testing Redis");
        tester.testDatabase();
        tester = new CassandraTester();
        System.out.println("Testing Cassandra");
        tester.testDatabase();
    }
}
