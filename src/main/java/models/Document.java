package models;

import java.util.List;

public class Document {

    public DocumentId docId;
    public List<String> pages;

    public Document(DocumentId docId, List<String> pages) {
        this.docId = docId;
        this.pages = pages;
    }
}
