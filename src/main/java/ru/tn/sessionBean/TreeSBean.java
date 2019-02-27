package ru.tn.sessionBean;

import ru.tn.model.TreeNodeModel;

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
public class TreeSBean {

    private static final String SQL = "select name, id, my_type from " +
            "table(site_users.sel_struct_filter2_1(?, ?, upper(?), ?, ?)) where parent = ?";

    @Resource(mappedName = "jdbc/OracleDataSource")
    private DataSource ds;

    /**
     *
     * @param objectTypeId -1 для поиска по всем типам
     * @param searchTypeId -1 для поиска по имени
     * @param searchText
     * @param userName
     * @param linkingTypeId 1 линкованные 0 не линкованные -1 все
     * @param parentNode
     */
    public List<TreeNodeModel> getTreeNode(long objectTypeId, long searchTypeId, String searchText,
                                           String userName, int linkingTypeId, String parentNode) {
        List<TreeNodeModel> result = new ArrayList<>();
        try(Connection connect = ds.getConnection();
            PreparedStatement stm = connect.prepareStatement(SQL)) {
            stm.setLong(1, objectTypeId);
            stm.setLong(2, searchTypeId);
            stm.setString(3, searchText);
            stm.setString(4, userName);
            stm.setInt(5, linkingTypeId);
            stm.setString(6, parentNode);

            ResultSet res = stm.executeQuery();
            while(res.next()) {
                result.add(new TreeNodeModel(res.getString(1), res.getString(2), res.getString(3)));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
