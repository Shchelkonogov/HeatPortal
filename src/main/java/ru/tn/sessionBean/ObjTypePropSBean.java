package ru.tn.sessionBean;

import ru.tn.entity.ObjTypePropertiesEntity;
import ru.tn.model.ObjTypePropertyModel;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Stateless
public class ObjTypePropSBean {

    private static final String DEFAULT_SEARCH_VALUE = "По имени";
    private static final long DEFAULT_SEARCH_ID = -1;

    @PersistenceContext(unitName = "OracleDB")
    private EntityManager em;

    public List<ObjTypePropertyModel> getObjTypeProps(long objTypeId) {
        List<ObjTypePropertyModel> result = new ArrayList<>(
                Collections.singletonList(new ObjTypePropertyModel(DEFAULT_SEARCH_VALUE, DEFAULT_SEARCH_ID)));
        for (ObjTypePropertiesEntity item:
                em.createNamedQuery("ObjTypePropertiesEntity.getAllProp", ObjTypePropertiesEntity.class)
                        .setParameter("id", objTypeId)
                        .getResultList()) {
            result.add(new ObjTypePropertyModel(item.getObjPropName(), item.getObjPropId()));
        }
        return result;
    }

    public long getDefaultSearchId() {
        return DEFAULT_SEARCH_ID;
    }
}
