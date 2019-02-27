package ru.tn.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class ObjTypePropertiesEntityPK implements Serializable {
    private long objTypeId;
    private long objPropId;

    @Column(name = "OBJ_TYPE_ID")
    @Id
    public long getObjTypeId() {
        return objTypeId;
    }

    public void setObjTypeId(long objTypeId) {
        this.objTypeId = objTypeId;
    }

    @Column(name = "OBJ_PROP_ID")
    @Id
    public long getObjPropId() {
        return objPropId;
    }

    public void setObjPropId(long objPropId) {
        this.objPropId = objPropId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjTypePropertiesEntityPK that = (ObjTypePropertiesEntityPK) o;
        return objTypeId == that.objTypeId &&
                objPropId == that.objPropId;
    }

    @Override
    public int hashCode() {

        return Objects.hash(objTypeId, objPropId);
    }
}
