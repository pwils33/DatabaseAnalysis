package mysql.daos;

import java.sql.*;

public class ConnectionWrapper {

    public void init() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    private Connection connection = null;
    private PreparedStatement stmt = null;
    private ResultSet rs = null;

    protected Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/product?"
                    + "user=software&password=software");
        }
        return connection;
    }

    protected PreparedStatement getStatement(String query) throws SQLException {
        stmt = getConnection().prepareStatement(query);
        return stmt;
    }

    protected ResultSet updateAndGetResultSet() throws SQLException {
        if (stmt != null) {
            rs = stmt.executeQuery();
        }
        return rs;
    }

    protected void close() {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (stmt != null) {
                stmt.close();
                stmt = null;
            }

            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {

        }
    }

}
