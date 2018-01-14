package gmsis.vehicles.logic;

import gmsis.Main;
import javafx.fxml.Initializable;
import gmsis.database.Database;
import gmsis.models.logic.HelpPopulate;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * VehiclesHomeFXML Controller class
 *
 * @author Tamanna Rahman
 */
public class VehiclesController implements Initializable{

    //Vehicle home page components
    @FXML private TextField searchBar;
    @FXML private Button editWarr;
    @FXML private CheckBox carCheck;
    @FXML private CheckBox vanCheck;  
    @FXML private CheckBox truckCheck;
    @FXML private TableView resultsTable;
    @FXML private TableColumn regColumn;
    @FXML private TableColumn firstNameColumn;
    @FXML private TableColumn surnameColumn;
    @FXML private TableColumn addressColumn;
    @FXML private TableColumn postcodeColumn;
    @FXML private TableColumn phoneColumn;
    @FXML private TableColumn emailColumn;
    @FXML private TableColumn nextBookedColumn;
    @FXML private TextArea details_list;
    @FXML private ListView bookings_list;
    @FXML private ListView parts_list;
    
    private Database db;
    
    /***************************************************************************************
    ** Method initialises the table containing the customer and vehicle registration number.
    ***************************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle rb) {  
        try {
            db = Database.getInstance();
            checkWarranty();
            populateTable(standard());
            resultsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    String regNum = getSelectedVehicle();
                    vehicleDetails(regNum);
                    bookingList(regNum);  
                    partsList(regNum);
                } catch (ParseException ex) {
                    Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (ParseException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    /********************************************************************************************************
    ** Method checks if the warranty of a vehicle has expired and updates the warranty table in the database
    ********************************************************************************************************/
    public void checkWarranty(){
        try {
            ResultSet result = db.query("SELECT underWarranty, WarExpiration FROM Vehicles WHERE underWarranty = 1 AND WarExpiration < Date('now') ");
            while(result.next()){
                db.update("UPDATE Vehicles SET underWarranty = 0 WHERE  WarExpiration < Date('now');");
            }
        } catch (SQLException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /************************************************************************
    ** Method allows the user to search using either the registration number 
    ** or the manufacturer and displays the result in the table
    ************************************************************************/
    public void pressSearch(KeyEvent keyEvent) throws ParseException{
        String searchResult = searchBar.getText();
        if(carCheck.isSelected()){
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles, Templates WHERE Vehicles.CustomerID = Customers.CustomerID AND Vehicles.TemplateID = Templates.ID AND Templates.Type = 'Car' AND (Vehicles.RegPlate LIKE '" + searchResult + "%' OR Templates.Make LIKE '" + searchResult + "%');");
            populateTable(result);
        } else if(vanCheck.isSelected()){
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles, Templates WHERE Vehicles.CustomerID = Customers.CustomerID AND Vehicles.TemplateID = Templates.ID AND Templates.Type = 'Van' AND (Vehicles.RegPlate LIKE '" + searchResult + "%' OR Templates.Make LIKE '" + searchResult + "%');");
            populateTable(result);
        } else if(truckCheck.isSelected()){
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles, Templates WHERE Vehicles.CustomerID = Customers.CustomerID AND Vehicles.TemplateID = Templates.ID AND Templates.Type = 'Truck' AND (Vehicles.RegPlate LIKE '" + searchResult + "%' OR Templates.Make LIKE '" + searchResult + "%');");
            populateTable(result);
        } else{
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles WHERE Customers.CustomerID = Vehicles.CustomerID AND (Vehicles.RegPlate LIKE '" + searchResult + "%' OR (SELECT Templates.ID FROM Templates WHERE Templates.Make LIKE '" + searchResult + "%' AND Templates.ID = Vehicles.TemplateID))");
            populateTable(result);
        }                 
    }
    
    /****************************************************
    ** Method allows user to search by vehicle type: car.
    ****************************************************/
    public void searchCar(ActionEvent event) throws ParseException{
        if(carCheck.isSelected()){
            vanCheck.setSelected(false);
            truckCheck.setSelected(false);
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles, Templates WHERE Vehicles.CustomerID = Customers.CustomerID AND Vehicles.TemplateID = Templates.ID AND Templates.Type = 'Car';");
            populateTable(result);
        } else{
            populateTable(standard());
        }
    }
    
    /****************************************************
    ** Method allows user to search by vehicle type: van.
    ****************************************************/
    public void searchVan(ActionEvent event) throws ParseException{
        if(vanCheck.isSelected()){
            carCheck.setSelected(false);
            truckCheck.setSelected(false);
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles, Templates WHERE Vehicles.CustomerID = Customers.CustomerID AND Vehicles.TemplateID = Templates.ID AND Templates.Type = 'Van';");
            populateTable(result);
        } else{
            populateTable(standard());
        }
    }
    
    /******************************************************
    ** Method allows user to search by vehicle type: truck.
    ******************************************************/
    public void searchTruck(ActionEvent event) throws ParseException{
        if(truckCheck.isSelected()){
            carCheck.setSelected(false);
            vanCheck.setSelected(false);
            ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles, Templates WHERE Vehicles.CustomerID = Customers.CustomerID AND Vehicles.TemplateID = Templates.ID AND Templates.Type = 'Truck';");
            populateTable(result);
        } else{
            populateTable(standard());
        }
    }
    
    /*********************************************************************************************************
    ** Opens up the add vehicle window if a customer has been selected from the table. 
    ** If a customer has not been selected then it will alert the user that they have not selected a customer.
    *********************************************************************************************************/
    public void pressAdd(ActionEvent event) throws IOException, SQLException, ParseException{
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("vehicles/gui/Add.fxml"));
        Stage stage = new Stage();
        stage.getIcons().add(new Image("gmsis/icon.png"));
        stage.initStyle(StageStyle.DECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setTitle("Add");  
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.showAndWait(); 
        populateTable(standard());
    }
    
    /********************************************************************************************************
    ** Opens up the edit vehicle window if a customer has been selected from the table. 
    ** If a customer has not been selected then it will alert the user that they have not selected a customer.
    *********************************************************************************************************/
    public void pressEdit(ActionEvent event) throws IOException, SQLException, ParseException{
        if(getSelectedVehicle() != null){
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("vehicles/gui/Edit.fxml"));
            Parent node = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Edit"); 
            stage.getIcons().add(new Image("gmsis/icon.png"));
            EditController controller = fxmlLoader.<EditController>getController();
            controller.setCustomerNVehicle(getSelectedVehicle(), getSelectedCustomer());
            stage.initStyle(StageStyle.DECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(node));
            stage.showAndWait(); 
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No vehicle has been selected");
            alert.setContentText("Please select a vehicle from the table to edit.");
            alert.showAndWait();
        }
        populateTable(standard());
    }
    
    /*******************************************************************
    ** Deletes the selected vehicle from the table. 
    ** If a vehicle has not been selected then the user will be alerted.
    *******************************************************************/
    public void pressDelete(ActionEvent event) throws IOException, ParseException {
        String regNum = getSelectedVehicle();
        if(regNum != null){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Deletion Confirmation");
            alert.setContentText("Are you sure you want to delete this vehicle?");
            Optional<ButtonType> but = alert.showAndWait();
            if(but.get() == ButtonType.OK){
                db.update("DELETE FROM SPCBooking WHERE VehicleID = '" + regNum + "';");
                db.update("DELETE FROM PartUpdates WHERE (SELECT Bookings.BookingID FROM Bookings WHERE Bookings.BookingID = PartUpdates.BookingID AND Bookings.RegPlate = '" + regNum + "')");
                db.update("DELETE FROM Bookings WHERE RegPlate = '" + regNum + "';");
                db.update("DELETE FROM Vehicles WHERE RegPlate = '" + regNum + "' AND (SELECT CustomerID FROM Customers WHERE Customers.CustomerID = Vehicles.CustomerID)");
                populateTable(standard()); 
            }      
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR); 
            alert.setHeaderText("No vehicle has been selected");
            alert.setContentText("Please select a vehicle from the table.");
            alert.showAndWait();
        }        
    }
    
    public void editWarranty(ActionEvent event) throws ParseException, IOException{
        if(getSelectedVehicle() != null){
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("vehicles/gui/WarrantyEdit.fxml"));
            Parent node = (Parent)fxmlLoader.load();
            Stage stage = new Stage();
            stage.getIcons().add(new Image("gmsis/icon.png"));
            stage.initStyle(StageStyle.DECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Edit Warranty");  
            WarrantyEditController controller = fxmlLoader.<WarrantyEditController>getController();
            controller.setCustomerNVehicle(getSelectedVehicle(), getSelectedCustomer());
            stage.setScene(new Scene(node));
            stage.showAndWait(); 
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No vehicle has been selected");
            alert.setContentText("Please select a vehicle from the table to edit.");
            alert.showAndWait();
        }
        populateTable(standard()); 
    }
      
    /*********************************************************************************
    ** Method adds the customer details, vehicle registration number and next booking 
    ** to the table using the given query.
    ** Credit: Taken from Connor's code
    **********************************************************************************/
    public void populateTable(ResultSet result) throws ParseException{
        resultsTable.getItems().clear();
        details_list.clear();
        bookings_list.getItems().clear();
        parts_list.getItems().clear();
        
        regColumn.setCellValueFactory(new PropertyValueFactory("RegNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory("Firstname"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory("Surname"));
        addressColumn.setCellValueFactory(new PropertyValueFactory("Address"));
        postcodeColumn.setCellValueFactory(new PropertyValueFactory("Postcode"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory("PhoneNo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory("Email"));
        nextBookedColumn.setCellValueFactory(new PropertyValueFactory("NextBooking"));

        try {
            ArrayList<HelpPopulate> list = new ArrayList<>();

            while(result.next()) {
                String nextBooking = getNextBooking(result.getString("RegPlate"));
                HelpPopulate row = new HelpPopulate(result.getString("RegPlate"), 
                                                     result.getInt("CustomerID"),
                                                     result.getString("Firstname"),
                                                     result.getString("Lastname"),
                                                     result.getString("Address Line 1") + " " + result.getString("Address Line 2") + ", " + result.getString("City") + ", " + result.getString("County"),
                                                     result.getString("Postcode"),
                                                     result.getString("Email"),
                                                     result.getString("PhoneNumber"),
                                                     nextBooking);
                list.add(row);
            }
            
            resultsTable.setItems(FXCollections.observableList(list));
            resultsTable.getSelectionModel().select(-1);
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    /*****************************************************************
    ** Gets the registration number of the selected item in the table.
    ** Credit: Taken from Connor's code
    ******************************************************************/
    public String getSelectedVehicle(){
        try {
            ResultSet result = db.query("SELECT underWarranty FROM Vehicles WHERE RegPlate = '" + ((HelpPopulate) resultsTable.getSelectionModel().getSelectedItem()).getRegNumber() + "';");
            try {
                if(result.getInt("underWarranty") == 1){
                    editWarr.setDisable(false);
                } else {
                    editWarr.setDisable(true);
                }
            } catch (SQLException ex) {
                Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return ((HelpPopulate) resultsTable.getSelectionModel().getSelectedItem()).getRegNumber();
        }catch (NullPointerException e) {
            return null;
        }
    }
    
    /***********************************************************
    ** Gets the customer ID of the selected item in the table.
    ** Credit: Taken from Connor's code
    ************************************************************/
    public int getSelectedCustomer(){
        try {
            return ((HelpPopulate) resultsTable.getSelectionModel().getSelectedItem()).getCustomerID();
        }catch (NullPointerException e) {
            return -1;
        }
    }

    /***********************************************************************
    ** Method contains the most frequently used query to populate the table
    ************************************************************************/
    public ResultSet standard(){
        ResultSet result = db.query("SELECT Vehicles.RegPlate, Customers.* FROM Customers, Vehicles WHERE Vehicles.CustomerID = Customers.CustomerID;");
        return result;
    }
    
    /************************************************************************************
    ** Method is used to get the next booking date for a vehicle. 
    ** It is converted to the format "dd-MM-yyyy" because that's the format I like it in.
    ** Then it is returned to the method populateTable.
    *************************************************************************************/
    public String getNextBooking(String reg) throws ParseException{     
        ResultSet result = db.query("SELECT Date FROM Bookings WHERE RegPlate = '" + reg + "' AND Date > DATE('now') ORDER BY DATE(date) ASC;");
        try {     
            String nextBooking = null;
            while(result.next()){
                if(result.getString("Date").equals("")){
                    break;
                } else {
                    Date date = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(result.getString("Date"));
                    nextBooking = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    break;
                }
            }
            return nextBooking;
        } catch (SQLException ex) {
           return null;
        }
    }
     
    /**************************************************************************************************
    ** Method is used to query the database for a vehicle's details and for it's warranty
    ** details if it has one. The ResultSet and registration number is passed to the method setDetails.
    **************************************************************************************************/
    public void vehicleDetails(String regNum) throws ParseException{
        try {
            ResultSet result = db.query("SELECT underWarranty From Vehicles WHERE RegPlate = '" + regNum + "';");
            while(result.next()){
                if(result.getInt("underWarranty") == 0){
                    result = db.query("SELECT Vehicles.*, Templates.* FROM Vehicles, Templates WHERE Vehicles.RegPlate = '" + regNum + "' AND Vehicles.TemplateID = Templates.ID;");
                    setDetails(result, regNum);
                }else{
                    result = db.query("SELECT Vehicles.*, Templates.*, Warranty.* FROM Vehicles, Templates, Warranty WHERE Vehicles.RegPlate = '" + regNum + "' AND Vehicles.TemplateID = Templates.ID AND Vehicles.WarrID = Warranty.WID;");
                    setDetails(result, regNum);
                }  
            }
                       
        } catch (SQLException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /***********************************
    ** Method sets the vehicle details.
    ***********************************/
    public void setDetails(ResultSet result, String regNum) throws ParseException{
        try{
            while(result.next()){
                if(result.getInt("underWarranty") == 0){
                  details_list.setText("Registration Number: " + regNum +
                                 "\nVehicle Type: " + result.getString("Type") +
                                 "\nMake: " + result.getString("Make") + 
                                 "\nModel: " + result.getString("Model") + 
                                 "\nEngine Size: " + result.getString("EngineSize") + 
                                 "\nFuel Type: " + result.getString("FuelType") + 
                                 "\nColour: " + result.getString("Colour") + 
                                 "\nMileage: " + result.getString("Mileage") + 
                                 "\nLast Service Date: " + result.getString("LastService") +
                                 "\nMoT Expiration Date: " + result.getString("MOTDate"));  
                }else{
                    Date date = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(result.getString("WarExpiration"));
                    String expiration = new SimpleDateFormat("dd/MM/yyyy").format(date);
                    details_list.setText("Registration Number: " + regNum +
                                 "\nVehicle Type: " + result.getString("Type") +
                                 "\nMake: " + result.getString("Make") + 
                                 "\nModel: " + result.getString("Model") + 
                                 "\nEngine Size: " + result.getString("EngineSize") + 
                                 "\nFuel Type: " + result.getString("FuelType") + 
                                 "\nColour: " + result.getString("Colour") + 
                                 "\nMileage: " + result.getString("Mileage") +
                                 "\nLast Service Date: " + result.getString("LastService") +
                                 "\nMoT Expiration Date: " + result.getString("MOTDate") + 
                                 "\n\nWarranty Details:\nCompany: " + result.getString("CName") + 
                                 "\nAddress: " + result.getString("Address") + ", " + result.getString("City") + ", " + result.getString("Country") + ", " + result.getString("Postcode") + 
                                 "\nWarranty Expiration: " + expiration); 
                }
                
            }
        }catch(SQLException e){
            Alert detailError = new Alert(Alert.AlertType.ERROR);
            detailError.setTitle("Error");
            detailError.setContentText("Vehicle details could not be found.");
        }
    }
    
    /**************************************************************************************
    ** Method gets a list of bookings for the selected vehicle and sets it in the listview.
    ***************************************************************************************/
    public void bookingList(String regNum) throws ParseException{
        ResultSet result = db.query("SELECT Date, TypeOfBooking, Status FROM Bookings WHERE RegPlate = '" + regNum + "' ORDER BY DATE(date) ASC;");
        try {     
            ArrayList<String> bookings = new ArrayList<>();
            while(result.next()){
                if(result.getString("Date").equals("")){
                    break;
                } else {
                    Date date = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(result.getString("Date"));
                    bookings.add("Date: " + new SimpleDateFormat("dd-MM-yyyy").format(date) + "\nBooking Type: " + result.getString("TypeOfBooking") + "\nTotal Cost: £" + calculateBill(regNum, result.getString("Date"), result.getString("TypeOfBooking")) + "\nStatus: " + result.getString("Status"));
                }
            }
            bookings_list.setItems(FXCollections.observableList(bookings));            
        } catch (SQLException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**********************************************
    ** Method calculates each bookings total cost.
    **********************************************/
    private String calculateBill(String regNum, String date, String bookingType){
        ResultSet result = db.query("SELECT sum(PartUpdates.Quantity * Parts.Price) AS Total FROM Parts, PartUpdates, Bookings WHERE Bookings.BookingID = PartUpdates.BookingID AND PartUpdates.PartID = Parts.ID AND Bookings.RegPlate ='" + regNum + "' AND Bookings.Date = '" + date + "';");
        double spcCost = getSPCCost(regNum, date);
        double bill;
        if(bookingType.equals("R&D")){
            bill = 70.00;
        } else{
            bill = 50.00;
        }
        try {
            if(result.getDouble("Total") > 0.00){
                bill += result.getDouble("Total") + spcCost;
            }  
        } catch (SQLException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String total = String.format("%.2f", bill);
        return total;
    }
    
    /********************************************************************************
    ** Helper method to calculate each bookings total cost. Gets SPC additional cost.
    ********************************************************************************/
    private double getSPCCost(String regNum, String date){
        ResultSet result = db.query("SELECT sum(SPCBooking.Cost) as Total FROM Bookings, SPCBooking WHERE Bookings.RegPlate = '" + regNum + "' AND Bookings.Date = '" + date + "' AND Bookings.BookingID = SPCBooking.BookingID;");
        double cost = 0.00;
        try {
            if(result.getDouble("Total") > 0.00){
               cost = result.getDouble("Total"); 
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cost;
    }
    
    /**********************************************************************************************
    ** Method gets a list of all parts used for the selected vehicle and stores it in the listview.
    **********************************************************************************************/
    public void partsList(String regNum) throws ParseException{
        try {
            ResultSet result = db.query("SELECT Bookings.Date, Parts.Name, Parts.Desc, PartUpdates.Quantity FROM Parts, PartUpdates, Bookings WHERE Bookings.BookingID = PartUpdates.BookingID AND PartUpdates.PartID = Parts.ID AND Bookings.RegPlate ='" + regNum + "' ORDER BY DATE(Bookings.Date) ASC;");
   
            ArrayList<String> parts = new ArrayList<>();
            while(result.next()){
                if(result.getString("Name").equals("")){
                    break;
                } else {
                    Date date = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(result.getString("Date"));
                    parts.add("Booking Date: " + new SimpleDateFormat("dd-MM-yyyy").format(date) + "\nName: " + result.getString("Name") + "\nDescription: " + result.getString("Desc") + "\nQuantity: " + result.getInt("Quantity"));
                }
            }
            parts_list.setItems(FXCollections.observableList(parts));
        } catch (SQLException ex) {
            Logger.getLogger(VehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
                
}