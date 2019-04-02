package ru.tn.model;

public class ArchiveGridDataM {

    private String name;
    private String techProc;
    private String si;
    private String min;
    private String max;
    private String result;

    public ArchiveGridDataM(String name, String techProc, String si, String min, String max, String result) {
        this.name = name;
        this.techProc = techProc;
        this.si = si;
        this.min = min;
        this.max = max;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTechProc() {
        return techProc;
    }

    public void setTechProc(String techProc) {
        this.techProc = techProc;
    }

    public String getSi() {
        return si;
    }

    public void setSi(String si) {
        this.si = si;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ArchiveGridDataM{" + "name='" + name + '\'' +
                ", techProc='" + techProc + '\'' +
                ", min='" + min + '\'' +
                ", max='" + max + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
