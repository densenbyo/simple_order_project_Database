package cz.cvut.fel.dbs.dbUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Mukan Atazhanov
 * Created on 11-May-21
 */

public class dbConnection {
    private static final String USERNAME = "db21_atazhmuk";
    private static final String PASSWORD = "St4hBw";
    private static final String CONN = "jdbc:postgresql://slon.felk.cvut.cz:5432/" + USERNAME;

    public static Connection getConnection(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(CONN, USERNAME, PASSWORD);
            System.out.println("Connected");
        } catch (SQLException e){
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return conn;
    }
}
