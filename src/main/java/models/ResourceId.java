package models;

import java.util.UUID;

public abstract class ResourceId {
    public UUID id;

    protected ResourceId(UUID id) {
        this.id = id;
    }
}
