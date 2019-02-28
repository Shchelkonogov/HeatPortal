package ru.tn.sessionBean;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Stateless bean, для получения данных заголовка страницы
 */
@Stateless
public class HeaderTemplateSBean {

    private static final String SQL = "select u_description from m_adm_users where u_name = ?";

    @Resource(mappedName = "jdbc/OracleDataSource")
    private DataSource ds;

    /**
     * Получение описения пользователя по его имени
     * @param userName имя пользователя
     * @return описание пользователя
     */
    public String getUserDescription(String userName) {
        try (Connection connect = ds.getConnection();
                PreparedStatement stm = connect.prepareStatement(SQL)) {
            stm.setString(1, userName);
            ResultSet res = stm.executeQuery();
            res.next();
            return res.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "НЕИЗВЕСТНЫЙ ПОЛЬЗОВАТЕЛЬ";
    }
}
