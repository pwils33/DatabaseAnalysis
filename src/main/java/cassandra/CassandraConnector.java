package cassandra;

import com.datastax.oss.driver.api.core.CqlSession;

public class CassandraConnector {

    private CqlSession session;

    private boolean isConnected = false;
    private String connector = "";

    public void connect(String connector) {
        if (!isConnected) {
            session = CqlSession.builder().build();
            this.connector = connector;
        }
    }

    public CqlSession getSession() {
        return this.session;
    }

    public void close(String closer) {
        if (connector.equals(closer)) {
            session.close();
            connector = "";
            isConnected = false;
        }
    }
}
