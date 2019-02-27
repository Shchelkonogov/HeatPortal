package ru.tn.sessionBean;

import ru.tn.entity.ObjTypeEntity;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ObjTypeSBean {

    @PersistenceContext(unitName = "OracleDB")
    private EntityManager em;

    public List<ObjTypeEntity> getTypes() {
        return em.createNamedQuery("ObjTypeEntity.getAllTypes", ObjTypeEntity.class).getResultList();
    }
}
