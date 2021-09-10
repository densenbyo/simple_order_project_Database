package cz.cvut.fel.dbs.orders;

import cz.cvut.fel.dbs.dbUtil.dbConnection;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author Mukan Atazhanov
 * Created on 16-May-21
 */

public class orders implements Initializable {

    Connection connection = dbConnection.getConnection();

    @FXML
    private TableView<orderList> showTable = new TableView<>();
    @FXML
    private Button closeBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Label status;

    public void initialize(URL url, ResourceBundle rb) {
        try {
            buildData();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //action on clicking close button to close stage
    @FXML
    public void closeWin(ActionEvent event){
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    //action on clicking to clear whole table in orders database
    @FXML
    public void clearOrder(ActionEvent event){
        try {
            Class.forName("org.postgresql.Driver");
            String query = "DELETE FROM orders";
            PreparedStatement statement = connection.prepareStatement(query);
            int update = statement.executeUpdate();
            statement.close();
            //if order db cleared so label text will be setted as "ORDER DATABASE WAS CLEARED"
            status.setText("ORDER DATABASE WAS CLEARED");
            status.setTextFill(Color.GREEN);
        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    //building tableview showTable
    //also here we create dynamic columns according to elements in query
    //so firstly we adding columns to showTable and only after we set items from ObservableList
    public void buildData() throws ClassNotFoundException, SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String query = "SELECT tablenum, workername, price, dishes FROM orders";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet set = statement.executeQuery();
            ObservableList dbData = FXCollections.observableArrayList(dataBaseArrayList(set));
            for(int i=0 ; i < set.getMetaData().getColumnCount(); i++) {
                TableColumn column = new TableColumn<>();
                switch (set.getMetaData().getColumnName(i + 1)) {
                    case "tablenum":
                        column.setText("Table Number");
                        column.setMinWidth(100);
                        break;
                    case "workername":
                        column.setText("Waiter Name");
                        column.setMinWidth(120);
                        break;
                    case "price":
                        column.setText("Price (in €)");
                        column.setMinWidth(100);
                        break;
                    case "dishes":
                        column.setText("Food");
                        column.setMinWidth(120);
                        break;
                    default:
                        //if column name in SQL Database is not found, then TableView column receive SQL Database current column name (not readable)
                        column.setText(set.getMetaData().getColumnName(i + 1));
                        break;
                }
                //Setting cell property value to correct variable from RestMenu class.
                column.setCellValueFactory(new PropertyValueFactory<>(set.getMetaData().getColumnName(i + 1)));
                showTable.getColumns().add(column);
            }
            //Filling up tableView with data
            showTable.setItems(dbData);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    //settings of orderList means configuration of setting data from dataBaseArrayList
    //that price will be IntegerProperty and dishes will be StringProperty and so on
    public class orderList {
        IntegerProperty tablenum = new SimpleIntegerProperty();
        StringProperty workername = new SimpleStringProperty();
        IntegerProperty price = new SimpleIntegerProperty();
        StringProperty dishes = new SimpleStringProperty();

        public IntegerProperty tablenumProperty() {
            return tablenum;
        }
        public StringProperty workernameProperty() {
            return workername;
        }
        public IntegerProperty priceProperty() {
            return price;
        }
        public StringProperty dishesProperty() {
            return dishes;
        }

        public orderList(int tableValue, String workerValue, int priceValue, String dishesValue) {
            tablenum.set(tableValue);
            workername.set(workerValue);
            price.set(priceValue);
            dishes.set(dishesValue);
        }
        orderList(){}
    }

    //extracting data from ResulSet to ArrayList
    private ArrayList dataBaseArrayList(ResultSet set) throws SQLException {
        ArrayList<orderList> data =  new ArrayList<>();
        while (set.next()) {
            orderList order = new orderList();
            order.tablenum.set(set.getInt("tablenum"));
            order.workername.set(set.getString("workername"));
            order.price.set(set.getInt("price"));
            order.dishes.set(set.getString("dishes"));
            data.add(order);
        }
        return data;
    }
}
