package cz.cvut.fel.dbs.orderCreator;

import cz.cvut.fel.dbs.dbUtil.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Mukan Atazhanov
 * Created on 11-May-21
 */

public class orderCreatorModel {
    Connection connection;
    public orderCreatorModel(){
        this.connection = dbConnection.getConnection();
        if(this.connection == null){
            System.exit(1);
        }
    }
    public boolean isConnected(){
        if(this.connection != null){
            System.out.println();
            return true;
        }else {
            return false;
        }
    }
}
