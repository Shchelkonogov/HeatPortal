package ru.tn.sessionBean.cache;

import ru.tn.model.archiveData.DataModel;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Startup
@Singleton
public class ObjectDataCacheSBean {

    // TODO Реализовать размер кеша, что бы в нем было не больше 100 записей например.
    //  сделать возможность принудительной очистки кеша

    private Map<Long, List<DataModel>> cache = new HashMap<>();

    public void putCache(long key, List<DataModel> value) {
        cache.put(key, value);
    }

    public List<DataModel> getCache(long key) {
        return cache.get(key);
    }

    public boolean containsCache(long key) {
        return cache.containsKey(key);
    }
}
