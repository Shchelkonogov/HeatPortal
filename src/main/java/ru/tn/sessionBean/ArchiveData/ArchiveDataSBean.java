package ru.tn.sessionBean.ArchiveData;

import ru.tn.model.archiveData.DataModel;
import ru.tn.model.archiveData.DataValueModel;
import ru.tn.model.archiveData.EnumJournal;
import ru.tn.sessionBean.cache.ObjectDataCacheSBean;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String SQL_DATA = "select a.par_id, a.stat_aggr, a.categ, a.dif_int, a.techproc_type_id, a.param_type_id, "+
            "a.par_code, a.par_memo, a.proc_name, a.measure_id, a.measure_name, "+
            "(select param_type_name from dz_param_type where param_type_id = a.param_type_id) as param_type_name "+
            "from table (dsp_0032t.get_obj_params(?)) a order by visible";
    private static final String SQL_PATH = "select dsp_0032t.get_obj_full_path(?) from dual";
    private static final String SQL_GET_ADDRESS = "select get_obj_address(?) from dual";

    private static final String SQL_GET_ENUM_JOURNAL = "select time_stamp, param_cond_name " +
            "from table (dsp_0032t.sel_journal_tab(?, ?, to_date(?, 'dd.mm.yyyy'), to_date(?, 'dd.mm.yyyy HH24'))) " +
            "order by time_stamp";

    @Resource(name = "jdbc/dataSource")
    private DataSource ds;

    @Resource(name = "mnemoUrl")
    private String mnemoUrl;

    @Resource
    private ManagedExecutorService mes;

    @EJB
    private ObjectDataCacheSBean objectDataCache;

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

        int keyIncrement = 0;
        if (LocalDate.parse(date, FORMATTER).equals(LocalDate.now())) {
            keyIncrement = LocalDateTime.now().getHour();
        }

        long key = (objectId + date + keyIncrement).hashCode();
        if (objectDataCache.containsCache(key)) {
            LOG.info("loadData key: " + key + " data from cache");
            return objectDataCache.getCache(key);
        }

        if (LocalDate.parse(date, FORMATTER).isAfter(LocalDate.now())) {
            return null;
        }

        List<DataModel> data = new ArrayList<>();
        try (Connection connect = ds.getConnection();
             PreparedStatement stm = connect.prepareStatement(SQL_DATA)) {
            stm.setInt(1, objectId);

            ResultSet res = stm.executeQuery();
            while (res.next()) {
                data.add(new DataModel(res.getInt("par_id"), res.getInt("stat_aggr"),
                        res.getString("categ").equals("A"), res.getString("par_memo"),
                        res.getString("proc_name"), res.getString("measure_name"),
                        res.getString("dif_int"), res.getInt("param_type_id"),
                        res.getString("param_type_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Старт потоков загрузки данных и min/max
        List<Future<DataValueModel[]>> dataList = new ArrayList<>();
        for (DataModel model : data) {
            ParamDataLoadThread task = new ParamDataLoadThread(model.getParamId(), model.getStatAgr(),
                    objectId, date, System.currentTimeMillis(), model.isAnalog());
            dataList.add(mes.submit(task));
        }

        //Старт потока загрузки температуры наружного воздуха
        Future<DataValueModel[]> outTemp = mes.submit(new OutdoorTemperatureThread(objectId, date));

        try {
            //Обрабатываем результаты данных параметров и считаем итоги
            for (int i = 0; i < dataList.size(); i++) {
                data.get(i).setData(dataList.get(i).get());
                data.get(i).calcResult();
            }

            //Добавляем параметр температура наружного воздуха и кладем туда данные
            data.add(0, new DataModel(true, "D", "Тгмц", "-", "°C", 1, "Температура"));
            data.get(0).setMin("-");
            data.get(0).setMax("-");
            data.get(0).setData(outTemp.get());
            data.get(0).calcResult();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //Выставляем min/max значения для отображения (берем первую колонку с данными)
        for (DataModel model: data) {
            if (model.getData()[0] != null) {
                model.setMin(model.getData()[0].getMin());
                model.setMax(model.getData()[0].getMax());
            }
        }

        objectDataCache.putCache(key, data);

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

    /**
     * Метод для получения информации по переключениям
     * состояний перечислимых параметров
     * @param objectId id объекта
     * @param paramId id параметра
     * @param startDate начальная дата в формате dd.MM.yyyy
     * @param endDate конечная дата dd.MM.yyyy HH
     * @return список данных по переключениям {@link EnumJournal}
     */
    public List<EnumJournal> getEnumJournal(int objectId, int paramId, String startDate, String endDate) {
        List<EnumJournal> result = new ArrayList<>();

        try (Connection connect  = ds.getConnection();
             PreparedStatement stm = connect.prepareStatement(SQL_GET_ENUM_JOURNAL)) {
            stm.setInt(1, objectId);
            stm.setInt(2, paramId);
            stm.setString(3, startDate);
            stm.setString(4, endDate);

            ResultSet res = stm.executeQuery();
            while (res.next()) {
                result.add(new EnumJournal(res.getString(1), res.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getMnemoUrl() {
        return mnemoUrl;
    }
}
