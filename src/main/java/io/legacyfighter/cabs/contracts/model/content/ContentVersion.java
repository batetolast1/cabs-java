package io.legacyfighter.cabs.contracts.model.content;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ContentVersion {

    private String version;

    protected ContentVersion() {
    }

    public ContentVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentVersion that = (ContentVersion) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
