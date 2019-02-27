package ru.tn.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "OBJ_TYPE", schema = "ADMIN")
@NamedQueries({
        @NamedQuery(name = "ObjTypeEntity.getAllTypes",
                query = "select new ObjTypeEntity(o.objTypeName, o.objTypeId) from ObjTypeEntity o order by o.objTypeName")
})
public class ObjTypeEntity {

    private String objTypeName;
    private String objTypeChar;
    private long objTypeId;

    public ObjTypeEntity() {
    }

    public ObjTypeEntity(String objTypeName, long objTypeId) {
        this.objTypeName = objTypeName;
        this.objTypeId = objTypeId;
    }

    @Basic
    @Column(name = "OBJ_TYPE_NAME")
    public String getObjTypeName() {
        return objTypeName;
    }

    public void setObjTypeName(String objTypeName) {
        this.objTypeName = objTypeName;
    }

    @Basic
    @Column(name = "OBJ_TYPE_CHAR")
    public String getObjTypeChar() {
        return objTypeChar;
    }

    public void setObjTypeChar(String objTypeChar) {
        this.objTypeChar = objTypeChar;
    }

    @Id
    @Column(name = "OBJ_TYPE_ID")
    public long getObjTypeId() {
        return objTypeId;
    }

    public void setObjTypeId(long objTypeId) {
        this.objTypeId = objTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjTypeEntity that = (ObjTypeEntity) o;
        return objTypeId == that.objTypeId &&
                Objects.equals(objTypeName, that.objTypeName) &&
                Objects.equals(objTypeChar, that.objTypeChar);
    }

    @Override
    public int hashCode() {

        return Objects.hash(objTypeName, objTypeChar, objTypeId);
    }
}
