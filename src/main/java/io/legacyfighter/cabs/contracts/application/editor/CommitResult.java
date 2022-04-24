package io.legacyfighter.cabs.contracts.application.editor;

import java.util.Objects;
import java.util.UUID;

public class CommitResult {

    public enum Result {
        FAILURE, SUCCESS
    }

    private final UUID contentId;
    private final Result result;

    public CommitResult(UUID contentId, Result result) {
        this.contentId = contentId;
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    public UUID getContentId() {
        return contentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitResult that = (CommitResult) o;
        return Objects.equals(contentId, that.contentId) && result == that.result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId, result);
    }
}
