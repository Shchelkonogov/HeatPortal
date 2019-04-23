package ru.tn.model.archiveData;

public class DataValueModel {

    private String value;
    private String color;

    public DataValueModel() {
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

    @Override
    public String toString() {
        return "DataValueModel{" +
                "value='" + value + '\'' +
                ", color=" + color +
                '}';
    }
}
