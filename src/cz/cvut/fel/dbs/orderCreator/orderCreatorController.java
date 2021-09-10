package cz.cvut.fel.dbs.orderCreator;

import cz.cvut.fel.dbs.dbUtil.dbConnection;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;



/**
 * @author Mukan Atazhanov
 * Created on 11-May-21
 */


public class orderCreatorController implements Initializable {

    orderCreatorModel orderModel = new orderCreatorModel();
    Connection connection = dbConnection.getConnection();

    //setting all element from scene
    @FXML
    private Label status;
    @FXML
    private Label insertInfo;
    @FXML
    private ComboBox<String> restName;
    @FXML
    private ComboBox<String> waiterName;
    @FXML
    private ComboBox<Integer> tableNum;
    @FXML
    private Button createBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    Button showBtn = new Button();

    @FXML
    private TableView<RestMenu> menuTable = new TableView<>();
    @FXML
    private TableView<RestMenu> orderTable = new TableView<>();
    ObservableList<RestMenu> orderList = FXCollections.observableArrayList();

    //initializing all elements from scene
    public void initialize(URL url, ResourceBundle rb) {

        //We dont have here dynamic table, so we just create static columns as Price and Food
        TableColumn price = new TableColumn("Price (in €)");
        price.setMinWidth(70);
        price.setCellValueFactory(new PropertyValueFactory<RestMenu, IntegerProperty>("price"));

        TableColumn dishes = new TableColumn("Food");
        dishes.setMinWidth(120);
        dishes.setCellValueFactory(new PropertyValueFactory<RestMenu, StringProperty>("dishes"));

        orderTable.getColumns().addAll(price, dishes);

        //Checking connection by calling isConnected
        if(this.orderModel.isConnected()){
            this.status.setText("Connected");
            this.status.setTextFill(Color.GREEN);
        }else{
            this.status.setText("Not connected");
            this.status.setTextFill(Color.RED);
        }
        try {
            this.waiterName.setItems(FXCollections.observableArrayList(getWorker()));
            this.restName.setItems(FXCollections.observableArrayList(getRest()));
            this.tableNum.setItems(FXCollections.observableArrayList(getTables()));
            buildData();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //adding food func from menuTable(left side table) to orderTable(right side table)
    //of course if element is selected in left side table
    @FXML
    public void addFood(ActionEvent event){
        RestMenu selection = menuTable.getSelectionModel().getSelectedItem();
        if(selection != null){
            orderList.add(selection);
        }
        orderTable.setItems(orderList);
        System.out.println("Food was added to Order Table");
    }

    //deleting food func from orderTable(right side table)
    //of course if element is selected in right side table
    @FXML
    public void delFood(ActionEvent event){
        RestMenu selection = orderTable.getSelectionModel().getSelectedItem();
        if(selection != null){
            if(orderList.size() != 0){
                orderList.remove(selection);
            }
        } else { System.out.println("Didnt select food to delete"); }
        orderTable.setItems(orderList);
    }
    //adding order func means that selected element in orderTable(right side table)
    //will be INSERT INTO orders table in DB
    //also here we check if something selected means if we didnt select anything from orderTable we cannot insert it to DB
    //also same logic for table number and waiter name

    @FXML
    public void addOrder(ActionEvent event){
        Integer tableN = tableNum.getSelectionModel().getSelectedItem();
        String waiterN = waiterName.getSelectionModel().getSelectedItem();
        RestMenu selection = orderTable.getSelectionModel().getSelectedItem();
        if(selection != null){
            if(tableN != null){
                if(waiterN != null){
                    try {
                        Class.forName("org.postgresql.Driver");
                        String query1 = "INSERT INTO orders(tablenum, workername, price, dishes) VALUES(?, ?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(query1);
                        statement.setInt(1, tableN);
                        statement.setString(2, waiterN);
                        statement.setInt(3, selection.price.getValue());
                        statement.setString(4, selection.dishes.getValue());
                        statement.execute();
                        System.out.println("Order inserted to DB");
                        insertInfo.setText("ORDER INSERTED INTO DATABASE");
                        insertInfo.setTextFill(Color.GREEN);
                    } catch (SQLException | ClassNotFoundException e){
                        e.printStackTrace();
                        System.out.println("Error occurred while inserted");
                    }
                }else { System.out.println("Didnt select waiter name"); }
            } else { System.out.println("Didnt select table number"); }
        } else {
            System.out.println("Didnt select anything");
        }
    }

    public void showOrders(ActionEvent event) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../orders/orders.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //add to table combobox data from db stul
    private List<Integer> getTables() throws SQLException {
        List<Integer> options = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            String query1 = "SELECT number FROM stul";
            PreparedStatement statement = connection.prepareStatement(query1);
            ResultSet set = statement.executeQuery();
            while(set.next()){
                options.add(set.getInt("number"));
            }
            statement.close();
            set.close();
            return options;
        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    //add to restaurant combobox data from db restaurant
    private List<String> getRest() throws SQLException {
        List<String> options = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            String query2 = "SELECT id, name FROM restaurant ";
            PreparedStatement statement = connection.prepareStatement(query2);
            ResultSet set = statement.executeQuery();
            while(set.next()){
                options.add(set.getString("name"));
            }
            statement.close();
            set.close();
            return options;
        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    //add to worker combobox data from db worker
    private List<String> getWorker() throws SQLException {
        List<String> options = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            String query3 = "SELECT workername FROM worker WHERE position = 'waiter'";
            PreparedStatement statement = connection.prepareStatement(query3);
            ResultSet set = statement.executeQuery();
            while(set.next()){
                options.add(set.getString("workername"));
            }
            statement.close();
            set.close();
            return options;
        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    //building tableview menuTable
    //also here we create dynamic columns according to elements in query
    //so firstly we adding columns to menuTable and only after we set items from ObservableList
    public void buildData() throws ClassNotFoundException, SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String query = "SELECT price, dishes FROM menu";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet set = statement.executeQuery();
            ObservableList dbData = FXCollections.observableArrayList(dataBaseArrayList(set));
            for(int i=0 ; i < set.getMetaData().getColumnCount(); i++) {
                TableColumn column = new TableColumn<>();
                switch (set.getMetaData().getColumnName(i + 1)) {
                    case "price":
                        column.setText("Price (in €)");
                        column.setMinWidth(70);
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
                menuTable.getColumns().add(column);
            }
            //Filling up tableView with data
            menuTable.setItems(dbData);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    //settings of RestMenu means configuration of setting data from dataBaseArrayList
    //that price will be IntegerProperty and dishes will be StringProperty
    public class RestMenu{
        IntegerProperty price = new SimpleIntegerProperty();
        StringProperty dishes = new SimpleStringProperty();

        public IntegerProperty priceProperty() {
            return price;
        }
        public StringProperty dishesProperty() {
            return dishes;
        }
        public RestMenu(int priceValue, String dishesValue) {
            price.set(priceValue);
            dishes.set(dishesValue);
        }
        RestMenu(){}
    }

    //extracting data from ResulSet to ArrayList
    private ArrayList dataBaseArrayList(ResultSet set) throws SQLException {
        ArrayList<RestMenu> data =  new ArrayList<>();
        while (set.next()) {
            RestMenu menu = new RestMenu();
            menu.price.set(set.getInt("price"));
            menu.dishes.set(set.getString("dishes"));
            data.add(menu);
        }
        return data;
    }
}