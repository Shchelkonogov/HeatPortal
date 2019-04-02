package ru.tn.sessionBean;

import ru.tn.entity.ObjTypeEntity;
import ru.tn.entity.ObjTypePropertiesEntity;
import ru.tn.model.ObjTypePropertyModel;
import ru.tn.model.TreeNodeModel;
import ru.tn.sessionBean.cache.ObjTypePropertyCacheSBean;
import ru.tn.sessionBean.cache.TreeCacheSBean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Stateless bean для работы с блоком навигации на форме
 */
@Stateless
public class NavigationSBean {

    private static final Logger LOG = Logger.getLogger(NavigationSBean.class.getName());

    private static final String SQL = "select name, id, my_type from " +
            "table(site_users.sel_struct_filter2_1(?, ?, upper(?), ?, ?)) where parent = ?";

    private static final String DEFAULT_SEARCH_VALUE = "По имени";
    private static final long DEFAULT_SEARCH_ID = -1;

    @Resource(mappedName = "jdbc/OracleDataSource")
    private DataSource ds;

    @EJB
    private TreeCacheSBean treeCache;
    @EJB
    private ObjTypePropertyCacheSBean objTypePropCache;

    @PersistenceContext(unitName = "OracleDB")
    private EntityManager em;

    /**
     * Метод выгружает из базы все типы объектов
     * @return массив типов объектов
     */
    public List<ObjTypeEntity> getTypes() {
        return em.createNamedQuery("ObjTypeEntity.getAllTypes", ObjTypeEntity.class).getResultList();
    }

    /**
     * Метод возвращает массив элементов ветки дерева
     * @param objectTypeId тип объекта (-1 все типы)
     * @param searchTypeId тип поиска (-1 поиск по имени)
     * @param searchText текст поиска
     * @param userName имя пользователя от которого строится дерево
     * @param linkingTypeId тип линковки объектов (1 линкованные, -1 не линкованные, 0 все)
     * @param parentNode id элемента родителя дерева
     * @return массив с элементами ветки
     */
    public List<TreeNodeModel> getTreeNode(long objectTypeId, long searchTypeId, String searchText,
                                           String userName, int linkingTypeId, String parentNode) {
        LOG.info("NavigationSBean.getTreeNode " + objectTypeId + " " + searchTypeId + " " + searchText + " " + userName +
                " " + linkingTypeId + " " + parentNode);
        int key = (objectTypeId + searchTypeId + searchText + userName + linkingTypeId + parentNode).hashCode();
        if (treeCache.containsKey(key)) {
            LOG.info("NavigationSBean.getTreeNode cache " + key);
            return treeCache.getTreeCache(key);
        }

        List<TreeNodeModel> result = new ArrayList<>();
        try(Connection connect = ds.getConnection();
            PreparedStatement stm = connect.prepareStatement(SQL)) {
            stm.setLong(1, objectTypeId);
            stm.setLong(2, searchTypeId);
            stm.setString(3, searchText);
            stm.setString(4, userName);
            stm.setInt(5, linkingTypeId);
            stm.setString(6, parentNode);

            ResultSet res = stm.executeQuery();
            while(res.next()) {
                result.add(new TreeNodeModel(res.getString(1), res.getString(2), res.getString(3)));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        LOG.info("NavigationSBean.getTreeNode load " + key);
        treeCache.putTreeCache(key, result);
        return result;
    }

    /**
     * Метод возвращает массив свойств объектов в соответствии с типом объекта.
     * Добавляется свойство поиска по имени
     * @param objTypeId типо объекта
     * @return массив свойст
     */
    public List<ObjTypePropertyModel> getObjTypeProps(long objTypeId) {
        if (objTypePropCache.containsKey(objTypeId)) {
            return objTypePropCache.getCache(objTypeId);
        }

        List<ObjTypePropertyModel> result = new ArrayList<>(
                Collections.singletonList(new ObjTypePropertyModel(DEFAULT_SEARCH_VALUE, DEFAULT_SEARCH_ID)));
        for (ObjTypePropertiesEntity item:
                em.createNamedQuery("ObjTypePropertiesEntity.getAllProp", ObjTypePropertiesEntity.class)
                        .setParameter("id", objTypeId)
                        .getResultList()) {
            result.add(new ObjTypePropertyModel(item.getObjPropName(), item.getObjPropId()));
        }

        objTypePropCache.putCache(objTypeId, result);
        return result;
    }

    public long getDefaultSearchId() {
        return DEFAULT_SEARCH_ID;
    }
}
