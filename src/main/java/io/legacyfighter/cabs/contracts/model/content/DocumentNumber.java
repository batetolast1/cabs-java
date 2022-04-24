package io.legacyfighter.cabs.contracts.model.content;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class DocumentNumber {

    private String number;

    protected DocumentNumber() {
    }

    public DocumentNumber(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentNumber that = (DocumentNumber) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
