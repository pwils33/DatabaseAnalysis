package mysql.daos;

import doas.DocumentDAO;
import models.Document;
import models.DocumentId;
import models.UserId;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MySQLDocumentDAO implements DocumentDAO {

    private ConnectionWrapper connectionWrapper;

    public MySQLDocumentDAO(ConnectionWrapper connectionWrapper) {
        this.connectionWrapper = connectionWrapper;
    }

    public void createDocumentStorage() {
        final String createDocumentIdsQuery =
                "CREATE TABLE document_ids (" +
                        "user_id TEXT NOT NULL," +
                        "document_id TEXT NOT NULL" +
                        ");";
        final String createPagesQuery =
                "CREATE TABLE pages (" +
                        "document_id TEXT NOT NULL," +
                        "page_number INT NOT NULL," +
                        "page_content TEXT" +
                        ");";
        try {
            connectionWrapper.getStatement(createDocumentIdsQuery).executeUpdate();
            connectionWrapper.getStatement(createPagesQuery).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
    }

    @Override
    public void deleteDocumentStorage() {
        final String dropDocumentIdsQuery = "DROP TABLE IF EXISTS document_ids;";
        final String dropPagesQuery = "DROP TABLE IF EXISTS pages;";
        try {
            connectionWrapper.getStatement(dropDocumentIdsQuery).executeUpdate();
            connectionWrapper.getStatement(dropPagesQuery).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
    }

    public Document getDocumentByDocumentId(DocumentId documentId) {
        final String selectQuery = "SELECT (page_content) FROM pages WHERE document_id=?";
        try {
            connectionWrapper.getStatement(selectQuery).setString(1, documentId.id.toString());
            ResultSet rs = connectionWrapper.updateAndGetResultSet();
            ArrayList<String> pages = new ArrayList<>();
            while (rs.next()) {
                pages.add(rs.getString("page_content"));
            }
            return new Document(documentId, pages);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
        return null;
    }

    public List<Document> getDocumentsByDocumentIds(List<DocumentId> documentIds) {
        ArrayList<Document> docs = new ArrayList<>();
        for (DocumentId docId: documentIds) {
            docs.add(getDocumentByDocumentId(docId));
        }
        return docs;
    }

    public List<Document> getDocumentsByUserId(UserId userId) {
        ArrayList<DocumentId> docIds = new ArrayList<>();
        final String selectQuery = "SELECT (document_id) FROM document_ids WHERE user_id=?";
        try {
            connectionWrapper.getStatement(selectQuery).setString(1, userId.id.toString());
            ResultSet rs = connectionWrapper.updateAndGetResultSet();
            while (rs.next()) {
                docIds.add(new DocumentId(UUID.fromString(rs.getString("document_id"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
        return getDocumentsByDocumentIds(docIds);
    }

    public boolean insertDocument(UserId userId, Document document) {
        final String insertQuery = "INSERT INTO document_ids VALUES (?,?)";
        try {
            PreparedStatement stmt = connectionWrapper.getStatement(insertQuery);
            stmt.setString(1, userId.id.toString());
            stmt.setString(2, document.docId.id.toString());
            stmt.executeUpdate();
            return insertDocumentPages(document.docId, document.pages);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            connectionWrapper.close();
        }
    }

    public boolean insertDocuments(Map<UserId, List<Document>> documents) {
        boolean allSuccess = true;
        for (UserId userId : documents.keySet()) {
            for (Document document: documents.get(userId)) {
                allSuccess = insertDocument(userId, document) && allSuccess;
            }
        }
        return allSuccess;
    }

    private boolean insertDocumentPages(DocumentId documentId, List<String> pages) throws SQLException {
        final String insertQuery = "INSERT INTO pages values (?,?,?)";
        PreparedStatement stmt = connectionWrapper.getStatement(insertQuery);
        for (int index = 0; index < pages.size(); index++) {
            stmt.setString(1, documentId.id.toString());
            stmt.setInt(2, index);
            stmt.setString(3, pages.get(index));
            stmt.executeUpdate();
        }
        return true;
    }
}
