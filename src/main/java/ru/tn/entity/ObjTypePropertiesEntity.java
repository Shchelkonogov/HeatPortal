package ru.tn.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "OBJ_TYPE_PROPERTIES", schema = "ADMIN")
@IdClass(ObjTypePropertiesEntityPK.class)
@NamedQueries({
        @NamedQuery(name = "ObjTypePropertiesEntity.getAllProp",
                query = "select new ObjTypePropertiesEntity(o.objPropId, o.objPropName)  from ObjTypePropertiesEntity o where o.objTypeId = :id order by o.objPropName")
})
public class ObjTypePropertiesEntity {

    private long objTypeId;
    private long objPropId;
    private String objPropName;
    private String comments;
    private String objPropDef;
    private Long nS;
    private Long dispayId;
    private Long gispassport;
    private Long gistooltip;
    private Long gistreename;
    private Long gisplanname;
    private Long gissearchname;

    public ObjTypePropertiesEntity() {
    }

    public ObjTypePropertiesEntity(long objPropId, String objPropName) {
        this.objPropId = objPropId;
        this.objPropName = objPropName;
    }

    @Id
    @Column(name = "OBJ_TYPE_ID")
    public long getObjTypeId() {
        return objTypeId;
    }

    public void setObjTypeId(long objTypeId) {
        this.objTypeId = objTypeId;
    }

    @Id
    @Column(name = "OBJ_PROP_ID")
    public long getObjPropId() {
        return objPropId;
    }

    public void setObjPropId(long objPropId) {
        this.objPropId = objPropId;
    }

    @Basic
    @Column(name = "OBJ_PROP_NAME")
    public String getObjPropName() {
        return objPropName;
    }

    public void setObjPropName(String objPropName) {
        this.objPropName = objPropName;
    }

    @Basic
    @Column(name = "COMMENTS")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Basic
    @Column(name = "OBJ_PROP_DEF")
    public String getObjPropDef() {
        return objPropDef;
    }

    public void setObjPropDef(String objPropDef) {
        this.objPropDef = objPropDef;
    }

    @Basic
    @Column(name = "N_S")
    public Long getnS() {
        return nS;
    }

    public void setnS(Long nS) {
        this.nS = nS;
    }

    @Basic
    @Column(name = "DISPAY_ID")
    public Long getDispayId() {
        return dispayId;
    }

    public void setDispayId(Long dispayId) {
        this.dispayId = dispayId;
    }

    @Basic
    @Column(name = "GISPASSPORT")
    public Long getGispassport() {
        return gispassport;
    }

    public void setGispassport(Long gispassport) {
        this.gispassport = gispassport;
    }

    @Basic
    @Column(name = "GISTOOLTIP")
    public Long getGistooltip() {
        return gistooltip;
    }

    public void setGistooltip(Long gistooltip) {
        this.gistooltip = gistooltip;
    }

    @Basic
    @Column(name = "GISTREENAME")
    public Long getGistreename() {
        return gistreename;
    }

    public void setGistreename(Long gistreename) {
        this.gistreename = gistreename;
    }

    @Basic
    @Column(name = "GISPLANNAME")
    public Long getGisplanname() {
        return gisplanname;
    }

    public void setGisplanname(Long gisplanname) {
        this.gisplanname = gisplanname;
    }

    @Basic
    @Column(name = "GISSEARCHNAME")
    public Long getGissearchname() {
        return gissearchname;
    }

    public void setGissearchname(Long gissearchname) {
        this.gissearchname = gissearchname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjTypePropertiesEntity that = (ObjTypePropertiesEntity) o;
        return objTypeId == that.objTypeId &&
                objPropId == that.objPropId &&
                Objects.equals(objPropName, that.objPropName) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(objPropDef, that.objPropDef) &&
                Objects.equals(nS, that.nS) &&
                Objects.equals(dispayId, that.dispayId) &&
                Objects.equals(gispassport, that.gispassport) &&
                Objects.equals(gistooltip, that.gistooltip) &&
                Objects.equals(gistreename, that.gistreename) &&
                Objects.equals(gisplanname, that.gisplanname) &&
                Objects.equals(gissearchname, that.gissearchname);
    }

    @Override
    public int hashCode() {

        return Objects.hash(objTypeId, objPropId, objPropName, comments, objPropDef, nS, dispayId, gispassport, gistooltip, gistreename, gisplanname, gissearchname);
    }
}
