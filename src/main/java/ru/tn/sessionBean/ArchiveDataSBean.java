package ru.tn.sessionBean;

import ru.tn.model.ArchiveGridDataM;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ArchiveDataSBean {

    private static final String SQL_DATA = "select par_id, stat_aggr, categ, dif_int, techproc_type_id, param_type_id, " +
            "par_code, par_memo, proc_name, measure_id, measure_name " +
            "from table (dsp_0032t.get_obj_params(?))";
    private static final String SQL_PATH = "select dsp_0032t.get_obj_full_path(?) from dual";

    @Resource(mappedName = "jdbc/OracleDataSource")
    private DataSource ds;

    public List<ArchiveGridDataM> loadData(int objectId) {
        List<ArchiveGridDataM> data = new ArrayList<>();
        try (Connection connect = ds.getConnection();
             PreparedStatement stm = connect.prepareStatement(SQL_DATA)) {
            stm.setInt(1, objectId);

            ResultSet res = stm.executeQuery();
            while (res.next()) {
                data.add(new ArchiveGridDataM(res.getString("par_memo"), res.getString("proc_name"), res.getString("measure_name"), null, null, null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

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
}
