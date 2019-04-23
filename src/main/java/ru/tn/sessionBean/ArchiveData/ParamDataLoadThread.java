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

public class ParamDataLoadThread implements Callable<DataValueModel[]> {

    private static final Logger LOG = Logger.getLogger(ParamDataLoadThread.class.getName());

    private String SQL_DATA = "SELECT par_value, to_char(time_stamp - 1/24, 'hh24'), color " +
            "FROM table (dsp_0032t.get_data_param(?, ?, ?, to_date(?, 'dd.mm.yyyy')))";

    private int paramId;
    private int statAggr;
    private int objectId;
    private String date;
    private long globalTime;

    public ParamDataLoadThread(int paramId, int statAggr, int objectId,
                               String date, long globalTime) {
        this.paramId = paramId;
        this.statAggr = statAggr;
        this.objectId = objectId;
        this.date = date;
        this.globalTime = globalTime;
    }

    @Override
    public DataValueModel[] call() throws Exception {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/dataSource");

        LOG.info("ParamDataLoadThread.call initial " + (System.currentTimeMillis() - globalTime));
        long timer = System.currentTimeMillis();

        DataValueModel[] result = new DataValueModel[31];

        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL_DATA)) {
            stm.setInt(1, objectId);
            stm.setInt(2, paramId);
            stm.setInt(3, statAggr);
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

                result[res.getInt(2)] = new DataValueModel(res.getString(1), color);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LOG.info("ParamDataLoadThread.call end " + (System.currentTimeMillis() - timer));
        return result;
    }
}
