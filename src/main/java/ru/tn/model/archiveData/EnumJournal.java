package ru.tn.model.archiveData;

import java.io.Serializable;
import java.util.StringJoiner;

public class EnumJournal implements Serializable {

    private String date;
    private String value;

    public EnumJournal(String date, String value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EnumJournal.class.getSimpleName() + "[", "]")
                .add("date='" + date + "'")
                .add("value='" + value + "'")
                .toString();
    }
}
