package ru.tn.model.archiveData;

public class ColumnModel {

    private String header;
    private String property;
    private String propertyColor;

    public ColumnModel(String header, String property, String propertyColor) {
        this.header = header;
        this.property = property;
        this.propertyColor = propertyColor;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getPropertyColor() {
        return propertyColor;
    }

    public void setPropertyColor(String propertyColor) {
        this.propertyColor = propertyColor;
    }

    @Override
    public String toString() {
        return "ColumnModel{" +
                "header='" + header + '\'' +
                ", property='" + property + '\'' +
                ", propertyColor='" + propertyColor + '\'' +
                '}';
    }
}
