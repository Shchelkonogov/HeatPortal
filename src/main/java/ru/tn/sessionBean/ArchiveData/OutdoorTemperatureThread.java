package ru.tn.sessionBean.ArchiveData;

import ru.tn.model.archiveData.DataValueModel;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Класс для загрузки данных по температуре наружного воздуха
 */
public class OutdoorTemperatureThread implements Callable<DataValueModel[]> {

    private static final Logger LOG = Logger.getLogger(ParamDataLoadThread.class.getName());

    private static final String SQL = "select tnv, to_char(time_stamp - 1/24, 'hh24'), color " +
            "from table (dsp_0032t.get_tnv(?, to_date(?, 'dd.mm.yyyy')))";

    private int objectId;
    private String date;

    /**
     * Конструктор для инициализации потока
     * @param objectId id объукта
     * @param date дата в формате dd.mm.yyyy
     */
    OutdoorTemperatureThread(int objectId, String date) {
        this.objectId = objectId;
        this.date = date;
    }

    @Override
    public DataValueModel[] call() throws Exception {
        long timer = System.currentTimeMillis();
        LOG.info("OutdoorTemperatureThread.call start");

        DataValueModel[] result = new DataValueModel[31];

        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/dataSource");

        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL)) {
            stm.setInt(1, objectId);
            stm.setString(2, date);

            ResultSet res = stm.executeQuery();
            while (res.next()) {
                result[res.getInt(2)] = ((res.getInt(3) == 2) ?
                        new DataValueModel(res.getString(1), "gray")
                        : new DataValueModel(res.getString(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                result[i] = new DataValueModel();
            }
        }
        LOG.info("OutdoorTemperatureThread.call end " + (System.currentTimeMillis() - timer));
        return result;
    }
}
