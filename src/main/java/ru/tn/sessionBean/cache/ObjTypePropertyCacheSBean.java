package ru.tn.sessionBean.cache;

import ru.tn.model.ObjTypePropertyModel;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton bean являющийся кешом для свойств объектов
 */
@Startup
@Singleton
public class ObjTypePropertyCacheSBean {

    //TODO Реализовать размер кеша, что бы в нем было не больше 100 записей например.
    //  сделать возможность принудительной очистки кеша

    private Map<Long, List<ObjTypePropertyModel>> cache = new HashMap<>();

    public void putCache(long key, List<ObjTypePropertyModel> value) {
        cache.put(key, value);
    }

    public List<ObjTypePropertyModel> getCache(long key) {
        return cache.get(key);
    }

    public boolean containsKey(long key) {
        return cache.containsKey(key);
    }
}
