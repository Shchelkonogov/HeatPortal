package ru.tn.sessionBean;

import ru.tn.entity.ObjTypeEntity;
import ru.tn.model.ObjTypePropertyModel;
import ru.tn.model.TreeNodeModel;
import ru.tn.sessionBean.cache.ObjTypePropertyCacheSBean;
import ru.tn.sessionBean.cache.TreeCacheSBean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Startup;
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
@Startup
@Stateless
public class NavigationSBean {

    private static final Logger LOG = Logger.getLogger(NavigationSBean.class.getName());

    private static final String SQL = "select name, id, my_icon " +
            "from (select * from table(DSP_0032T.sel_struct_filter2_1_obh(?, ?, ?, ?))) " +
            "where level = nvl(2, level) " +
            "connect by prior id = parent start with id = ? " +
            "order siblings by order_by_struct(name, my_type)";

    private static final String SQL_OBJECT_TYPE_PROPERTY = "select obj_prop_id, obj_prop_name " +
            "from table(dsp_0032t.get_obj_type_props(?))";

    private static final String SQL_DEFAULT_OBJECT_TYPE_ID = "select obj_type_def from dz_sys_param";

    @Resource(name = "jdbc/dataSource")
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
     * @param parentNode id элемента родителя дерева
     * @return массив с элементами ветки
     */
    public List<TreeNodeModel> getTreeNode(long objectTypeId, long searchTypeId, String searchText,
                                           String userName, String parentNode) {
        LOG.info("NavigationSBean.getTreeNode " + objectTypeId + " " + searchTypeId + " " + searchText +
                " " + userName + " " + parentNode);
        int key = (objectTypeId + searchTypeId + searchText + userName + parentNode).hashCode();
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
            stm.setString(5, parentNode);

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
     * @param objTypeId тип объекта
     * @return массив свойств
     */
    public List<ObjTypePropertyModel> getObjTypeProps(long objTypeId) {
        LOG.info("NavigationSBean.getObjTypeProps start " + objTypeId);
        if (objTypePropCache.containsKey(objTypeId)) {
            LOG.info("NavigationSBean.getObjTypeProps cache");
            return objTypePropCache.getCache(objTypeId);
        }

        List<ObjTypePropertyModel> result = new ArrayList<>();

        try(Connection connect = ds.getConnection();
            PreparedStatement stm = connect.prepareStatement(SQL_OBJECT_TYPE_PROPERTY)) {
            stm.setLong(1, objTypeId);

            ResultSet res = stm.executeQuery();
            while (res.next()) {
                result.add(new ObjTypePropertyModel(res.getString(2), res.getLong(1)));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        objTypePropCache.putCache(objTypeId, result);
        LOG.info("NavigationSBean.getObjTypeProps load");
        return result;
    }

    /**
     * Метод возращает default тип объекта
     * @return id типа объекта
     * @throws Exception если произошла ошибка в базе
     */
    public int getDefaultObjectTypeId() throws Exception {
        LOG.info("NavigationSBean.getDefaultObjectTypeId");
        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL_DEFAULT_OBJECT_TYPE_ID)) {
            ResultSet res = stm.executeQuery();
            res.next();

            LOG.info("NavigationSBean.getDefaultObjectTypeId load");
            return res.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new Exception();
    }
}
