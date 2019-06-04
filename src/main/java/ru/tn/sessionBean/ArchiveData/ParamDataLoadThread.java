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
 * Класс для загрузки данных по параметрам и загрузки границ
 */
public class ParamDataLoadThread implements Callable<DataValueModel[]> {

    private static final Logger LOG = Logger.getLogger(ParamDataLoadThread.class.getName());

    private static final int DATA_SIZE = 31;

    private static final String SQL_DATA = "SELECT par_value, to_char(time_stamp - 1/24, 'hh24'), color " +
            "FROM table (dsp_0032t.get_data_param(?, ?, ?, to_date(?, 'dd.mm.yyyy')))";
    private static final String SQL_ALTER_SESSION = "alter session set NLS_NUMERIC_CHARACTERS='.,'";
    private StringBuilder sqlMinMax;

    private int paramId;
    private int statAggregate;
    private int objectId;
    private String date;
    private long globalTime;
    private boolean analog;

    /**
     * Конструктор для инициализации потока
     * @param paramId id параметра
     * @param statAggregate стат агрегат параметра
     * @param objectId id объекта
     * @param date дата в формате dd.mm.yyyy
     * @param globalTime время для логов, отдаю сюда System.currentTimeMillis
     * @param analog статус параметра (аналоговый или перечислимый)
     */
    ParamDataLoadThread(int paramId, int statAggregate, int objectId,
                        String date, long globalTime, boolean analog) {
        this.paramId = paramId;
        this.statAggregate = statAggregate;
        this.objectId = objectId;
        this.date = date;
        this.globalTime = globalTime;
        this.analog = analog;

        sqlMinMax = new StringBuilder("select ");
        for (int i = 1; i <= 24; i++) {
            sqlMinMax.append("dsp_0032t.get_limit(?, ?, ?, to_date(?, 'dd.mm.yyyy hh24') + ").append(i).append("/24, 309), ");
            sqlMinMax.append("dsp_0032t.get_limit(?, ?, ?, to_date(?, 'dd.mm.yyyy hh24') + ").append(i).append("/24, 308)");

            if (i != 24) {
                sqlMinMax.append(',');
            }
            sqlMinMax.append(' ');
        }
        sqlMinMax.append("from dual");
    }

    @Override
    public DataValueModel[] call() throws Exception {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/dataSource");

        LOG.info("ParamDataLoadThread.call initial " + (System.currentTimeMillis() - globalTime));
        long timer = System.currentTimeMillis();

        DataValueModel[] result = new DataValueModel[DATA_SIZE];
        for (int i = 0; i < DATA_SIZE; i++) {
            result[i] = new DataValueModel();
        }

        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL_DATA);
                PreparedStatement stmAlter = connect.prepareStatement(SQL_ALTER_SESSION);
                PreparedStatement stmMinMax = connect.prepareStatement(sqlMinMax.toString())) {
            stm.setInt(1, objectId);
            stm.setInt(2, paramId);
            stm.setInt(3, statAggregate);
            stm.setString(4, date);

            ResultSet res = stm.executeQuery();
            String color;
            while (res.next()) {
                switch (res.getInt(3)) {
                    case 1: {
                        color = "yellow";
                        break;
                    }
                    case 2: {
                        color = "gray";
                        break;
                    }
                    case 3: {
                        color = "red";
                        break;
                    }
                    default: color = "none";
                }

                int index = res.getInt(2);
                result[index].setValue(res.getString(1));
                result[index].setColor(color);
            }

            if (analog) {
                stmAlter.executeQuery();
                int index = 1;
                for (int i = 1; i <= 24; i++) {
                    stmMinMax.setInt(index, objectId);
                    stmMinMax.setInt(index + 1, paramId);
                    stmMinMax.setInt(index + 2, statAggregate);
                    stmMinMax.setString(index + 3, date + " 00");

                    stmMinMax.setInt(index + 4, objectId);
                    stmMinMax.setInt(index + 5, paramId);
                    stmMinMax.setInt(index + 6, statAggregate);
                    stmMinMax.setString(index + 7, date + " 00");

                    index += 8;
                }

                res = stmMinMax.executeQuery();
                if (res.next()) {
                    index = 1;
                    for (int i = 0; i < 24; i++) {
                        result[i].setMin(res.getString(index) == null ? "-" : res.getString(index));
                        result[i].setMax(res.getString(index + 1) == null ? "-" : res.getString(index + 1));
                        index += 2;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LOG.info("ParamDataLoadThread.call end " + (System.currentTimeMillis() - timer));
        return result;
    }
}
