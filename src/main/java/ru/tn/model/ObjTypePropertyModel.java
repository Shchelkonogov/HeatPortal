package ru.tn.model;

public class ObjTypePropertyModel {

    private String objTypeValue;
    private long objTypeId;

    public ObjTypePropertyModel(String objTypeValue, long objTypeId) {
        this.objTypeValue = objTypeValue;
        this.objTypeId = objTypeId;
    }

    public String getObjTypeValue() {
        return objTypeValue;
    }

    public long getObjTypeId() {
        return objTypeId;
    }

    @Override
    public String toString() {
        return "ObjTypePropertyModel{" + "objTypeValue='" + objTypeValue + '\'' +
                ", objTypeId=" + objTypeId +
                '}';
    }
}
