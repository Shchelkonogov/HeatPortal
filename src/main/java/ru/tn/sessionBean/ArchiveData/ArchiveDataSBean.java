package ru.tn.sessionBean.ArchiveData;

import ru.tn.model.archiveData.DataModel;
import ru.tn.model.archiveData.DataValueModel;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Stateless bean для работы архивными данными
 * и всеми закладками
 */
@Startup
@Stateless
public class ArchiveDataSBean {

    private static final Logger LOG = Logger.getLogger(ArchiveDataSBean.class.getName());

    private static final String SQL_DATA = "select par_id, stat_aggr, categ, dif_int, techproc_type_id, param_type_id, " +
            "par_code, par_memo, proc_name, measure_id, measure_name " +
            "from table (dsp_0032t.get_obj_params(?)) order by visible";
    private static final String SQL_PATH = "select dsp_0032t.get_obj_full_path(?) from dual";
    private static final String SQL_GET_ADDRESS = "select get_obj_address(?) from dual";

    @Resource(name = "jdbc/dataSource")
    private DataSource ds;

    @EJB
    private MinMax bean;

    @Resource(name = "mnemoUrl")
    private String mnemoUrl;

    @Resource
    private ManagedExecutorService mes;

    /**
     * Метод возвращает географический адрес объекта
     * @param objectId id объекта
     * @return географический адрес
     */
    public String getAddress(int objectId) {
        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL_GET_ADDRESS)) {
            stm.setInt(1, objectId);
            ResultSet res = stm.executeQuery();
            res.next();
            return res.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Метод выгружает данные по объекту для таблицы архивные данные
     * @param objectId id объекта
     * @param date дата за которую выгружаются данные
     * @return список данных
     */
    public List<DataModel> loadData(int objectId, String date) {
        LOG.info("ArchiveDataSBean.loadData objectId: " + objectId);
        long timer = System.currentTimeMillis();

        List<DataModel> data = new ArrayList<>();
        try (Connection connect = ds.getConnection();
             PreparedStatement stm = connect.prepareStatement(SQL_DATA)) {
            stm.setInt(1, objectId);

            ResultSet res = stm.executeQuery();
            while (res.next()) {
                data.add(new DataModel(res.getInt("par_id"), res.getInt("stat_aggr"),
                        res.getString("categ").equals("A"), res.getString("par_memo"),
                        res.getString("proc_name"), res.getString("measure_name"),
                        res.getString("dif_int")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Future<Void> handle = bean.loadMinMax(data, objectId, 1, date);

        List<Future<DataValueModel[]>> dataList = new ArrayList<>();
        for (DataModel model : data) {
            ParamDataLoadThread task = new ParamDataLoadThread(model.getParamId(), model.getStatAgr(),
                    objectId, date, System.currentTimeMillis());
            dataList.add(mes.submit(task));
        }

        try {
            handle.get();

            for (int i = 0; i < dataList.size(); i++) {
                data.get(i).setData(dataList.get(i).get());
                data.get(i).calcResult();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        LOG.info("ArchiveDataSBean.loadData " + (System.currentTimeMillis() - timer) + " " + data);
        return data;
    }

    /**
     * Метод возвращает путь позицию объекта в организационной
     * структуре и географический адрес
     * @param objectId id объекта
     * @return строка результата
     */
    public String getObjectPath(String objectId) {
        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL_PATH)) {
            stm.setInt(1, Integer.parseInt(objectId.substring(1)));

            ResultSet res = stm.executeQuery();
            res.next();
            return res.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMnemoUrl() {
        return mnemoUrl;
    }
}
