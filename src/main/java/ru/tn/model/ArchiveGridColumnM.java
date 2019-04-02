package ru.tn.model;

public class ArchiveGridColumnM {

    private String header;
    private String property;

    public ArchiveGridColumnM(String header, String property) {
        this.header = header;
        this.property = property;
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

    @Override
    public String toString() {
        return "ArchiveGridColumnM{" + "header='" + header + '\'' +
                ", property='" + property + '\'' +
                '}';
    }
}
