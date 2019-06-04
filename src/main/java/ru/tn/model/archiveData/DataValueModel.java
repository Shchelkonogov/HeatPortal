package ru.tn.model.archiveData;

import java.io.Serializable;
import java.util.StringJoiner;

public class DataValueModel implements Serializable {

    private String value;
    private String color = "none";
    private String min = "-";
    private String max = "-";

    public DataValueModel() {
    }

    public DataValueModel(String value) {
        this.value = value;
    }

    public DataValueModel(String value, String color) {
        this.value = value;
        this.color = color;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DataValueModel.class.getSimpleName() + "[", "]")
                .add("value='" + value + "'")
                .add("color='" + color + "'")
                .add("min='" + min + "'")
                .add("max='" + max + "'")
                .toString();
    }
}
