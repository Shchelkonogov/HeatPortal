package ru.tn.sessionBean.cache;

import ru.tn.model.TreeNodeModel;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton bean являющийся кешом для дерева объектов
 */
@Startup
@Singleton
public class TreeCacheSBean {

    //TODO Реализовать размер кеша, что бы в нем было не больше 100 записей например.
    //  сделали 2 поля для обновления деревьев
    //  DZ_SYS_PARAM.REFRESH_STRUCT_TREE - для дерева оргструктуры
    //  DZ_SYS_PARAM.REFRESH_ADDR_TREE - для адресного дерева
    //  если там лежит 1 - значит надо обновить кэши и положить тудо 0
    //  сделать возможность принудительной очистки кеша

    private Map<Integer, List<TreeNodeModel>> treeCache = new HashMap<>();

    public List<TreeNodeModel> getTreeCache(Integer key) {
        return treeCache.get(key);
    }

    public void putTreeCache(Integer key, List<TreeNodeModel> value) {
        treeCache.put(key, value);
    }

    public boolean containsKey(int key) {
        return treeCache.containsKey(key);
    }
}
