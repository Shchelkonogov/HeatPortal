package ru.tn.model.archiveData;

import java.io.Serializable;
import java.util.StringJoiner;

public class ParamType implements Serializable, Comparable<ParamType> {

    private int id;
    private String value;

    public ParamType(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ParamType.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("value='" + value + "'")
                .toString();
    }

    @Override
    public int compareTo(ParamType o) {
        return value.compareTo(o.value);
    }
}
