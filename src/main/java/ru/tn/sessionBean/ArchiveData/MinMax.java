package ru.tn.sessionBean.ArchiveData;

import ru.tn.model.archiveData.DataModel;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Startup
@Stateless
public class MinMax {

    private static final Logger LOG = Logger.getLogger(MinMax.class.getName());

    private static final String SQL_ALTER_SESSION = "alter session set NLS_NUMERIC_CHARACTERS='.,'";
    private static final String SQL_MIN_MAX = "SELECT dsp_0032t.get_limit(?, ?, ?, to_date(?, 'dd.mm.yyyy'), 309), " +
            "dsp_0032t.get_limit(?, ?, ?, to_date(?, 'dd.mm.yyyy'), 308) from dual";

    @Resource(name = "jdbc/dataSource")
    private DataSource ds;

    @Asynchronous
    public Future<Void> loadMinMax(List<DataModel> data, int objectId, int id, String date) {
        LOG.info("MinMax.loadMinMax start " + id);
        try (Connection connect = ds.getConnection();
                PreparedStatement alterSession = connect.prepareStatement(SQL_ALTER_SESSION);
                PreparedStatement stm = connect.prepareStatement(SQL_MIN_MAX)) {
            alterSession.executeQuery();

            for (DataModel param: data) {
                stm.setInt(1, objectId);
                stm.setInt(2, param.getParamId());
                stm.setInt(3, param.getStatAgr());
                stm.setString(4, date);

                stm.setInt(5, objectId);
                stm.setInt(6, param.getParamId());
                stm.setInt(7, param.getStatAgr());
                stm.setString(8, date);

                ResultSet res = stm.executeQuery();
                while (res.next()) {
                    param.setMin(res.getString(1) == null ? "-" : res.getString(1));
                    param.setMax(res.getString(2) == null ? "-" : res.getString(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LOG.info("MinMax.loadMinMax " + id + " " + data);
        LOG.info("MinMax.loadMinMax end " + id);
        return new AsyncResult<>(null);
    }


}
