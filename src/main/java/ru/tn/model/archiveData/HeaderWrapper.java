package ru.tn.model.archiveData;

import java.io.Serializable;
import java.util.StringJoiner;

public class HeaderWrapper implements Serializable {

    private String name;
    private int columnCount = 1;

    public HeaderWrapper(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void incrementColumnCount() {
        columnCount++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HeaderWrapper) {
            return this.name.equals(((HeaderWrapper) obj).name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HeaderWrapper.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("columnCount=" + columnCount)
                .toString();
    }
}
