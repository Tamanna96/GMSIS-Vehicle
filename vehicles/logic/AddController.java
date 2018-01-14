package gmsis.vehicles.logic;

import gmsis.database.Database;
import gmsis.models.logic.Customers;
import gmsis.models.logic.Vehicle;
import gmsis.models.logic.VehicleTemplate;
import gmsis.models.logic.WarrantyCompany;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * AddFXML Controller class
 *
 * @author Tamanna Rahman
 */
public class AddController implements Initializable {

    @FXML private Button cancel;
    @FXML private Button add;

    //Customer details section components
    @FXML private ComboBox customers;
    @FXML private Label selectCustomer;
    @FXML private Label custErr;
    
    //Vehicle details section components
    @FXML private ComboBox vType;
    @FXML private TextField regNum;
    @FXML private ComboBox make;
    @FXML private ComboBox model;
    @FXML private ComboBox engSize;
    @FXML private ComboBox fType;
    @FXML private TextField colour;
    @FXML private TextField cMile;
    @FXML private DatePicker MoTDate;
    @FXML private DatePicker lastService;
    @FXML private CheckBox warranty;

    //Warranty details section components
    @FXML private AnchorPane warrantyDets;
    @FXML private ComboBox companies;
    @FXML private TextField cName;
    @FXML private TextField add1;
    @FXML private TextField city;
    @FXML private TextField country;
    @FXML private TextField postcode;
    @FXML private DatePicker expDate;

    //Error labels for vehicle details part
    @FXML private Label vehErr;
    @FXML private Label typeErr;
    @FXML private Label regErr;
    @FXML private Label makeErr;
    @FXML private Label modelErr;
    @FXML private Label engErr;
    @FXML private Label fuelErr;
    @FXML private Label colErr;
    @FXML private Label cmileErr;
    @FXML private Label motErr;
    @FXML private Label lastErr;

    //Error labels for warranty details part
    @FXML private Label warErr;
    @FXML private Label nameErr;
    @FXML private Label addErr;
    @FXML private Label cityErr;
    @FXML private Label countryErr;
    @FXML private Label pcErr;
    @FXML private Label expErr;

    private Database db;
    private Vehicle veh;

    private int warrantyStatus;
    private String warExpiration;
    private int warrantyID;

    private int customer;
    private int template;
    private String type;
    private String vmake;
    private String vmodel;
    private String eSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Locale.setDefault(Locale.UK);
            db = Database.getInstance();
            warrantyID = -1;
            template = -1;
            customers.setItems(FXCollections.observableList(getCustomers()));
            companies.setItems(FXCollections.observableList(getCompanies()));
            warrantyDets.setDisable(true);
            vType.setItems(FXCollections.observableArrayList("Car", "Truck", "Van"));
            make.setItems(FXCollections.observableArrayList(setMake(initialCombo("Make"))));
            model.setItems(FXCollections.observableArrayList(setModel(initialCombo("Model"))));
            engSize.setItems(FXCollections.observableArrayList(setEng(initialCombo("EngineSize"))));
            fType.setItems(FXCollections.observableArrayList(setFuel(initialCombo("FuelType"))));
            MoTDate.setDayCellFactory(beforeDate());
            lastService.setDayCellFactory(afterDate());
        } catch (UnsupportedOperationException e) {
            System.out.println("Not supported yet.");
        }
    }

    /******************************************************************************************
    ** Credit: https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm
    ******************************************************************************************/
    private Callback<DatePicker, DateCell> beforeDate() {
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        return dayCellFactory;
    }

    /******************************************************************************************
    ** Credit: https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm
    ******************************************************************************************/
    private Callback<DatePicker, DateCell> afterDate() {
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isAfter(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        return dayCellFactory;
    }
    
    /***********************************************************************************
    ** Method clears the vehicle details section when clear for that section is pressed.
    ***********************************************************************************/
    public void pressClear1(ActionEvent event) {
        vType.setValue(null);
        regNum.clear();
        make.setValue(null);
        model.setValue(null);
        engSize.setValue(null);
        fType.setValue(null);
        colour.clear();
        cMile.clear();
        MoTDate.getEditor().clear();
        lastService.getEditor().clear();
        warranty.setSelected(false);
        warrantyDets.setDisable(true);
        typeErr.setVisible(false);
        regErr.setVisible(false);
        makeErr.setVisible(false);
        modelErr.setVisible(false);
        engErr.setVisible(false);
        fuelErr.setVisible(false);
        colErr.setVisible(false);
        cmileErr.setVisible(false);
        motErr.setVisible(false);
        vehErr.setVisible(false);
    }
    
    /***********************************************************************************
    ** Method clears the warranty details section when clear for that section is pressed.
    ***********************************************************************************/
    public void pressClear2(ActionEvent event){
        companies.setValue(null);
        cName.clear();
        add1.clear();
        city.clear();
        country.clear();
        postcode.clear();
        expDate.getEditor().clear(); 
        warErr.setVisible(false);
        nameErr.setVisible(false);
        addErr.setVisible(false);
        cityErr.setVisible(false);
        countryErr.setVisible(false);
        pcErr.setVisible(false);
        expErr.setVisible(false);
    }

    /*******************************************************
    ** Method closes the add window when cancel is pressed.
    *******************************************************/
    public void pressCancel(ActionEvent event) throws IOException{
        Stage stage = (Stage) cancel.getScene().getWindow();          
        stage.close();                     
    }
    
    /*************************************************************
    ** Method adds the vehicle to the database if no errors occur.
    **************************************************************/
    public void pressAdd(ActionEvent event) throws IOException, SQLException{
        boolean check = checkDetails();    
        if(check == true){
            vehErr.setVisible(false);
            VehicleTemplate temp = new VehicleTemplate(vType.getValue().toString(), make.getValue().toString(), model.getValue().toString(), engSize.getValue().toString(), fType.getValue().toString());
            ResultSet result = db.query("SELECT ID FROM Templates WHERE Type = '" + temp.getType() + "' AND Make = '" + temp.getMake() + "' AND Model = '" + temp.getModel() + "' AND EngineSize = '" + temp.getEngineSize() + "' AND FuelType = '" + temp.getFuelType() + "';");
            if(result.next()){
                setTemplateID(result.getInt("ID"));
            } else{
                addTemplate(temp);
            }
            if(!warranty.isSelected()){
                warrantyStatus = 0;
                warExpiration = null;
                helpAdd();
            } else{
                boolean checkw = checkWarranty();
                if(checkw == true){
                    warErr.setVisible(false);
                    warrantyStatus = 1;
                    WarrantyCompany wc = new WarrantyCompany(cName.getText(),add1.getText(), city.getText(), country.getText(), postcode.getText());
                    warExpiration = expDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if(getWarrantyID() == -1){
                        addWarranty(wc);
                    } 
                    helpAdd();
                } else{
                   warErr.setVisible(true); 
                }
            }
        } else{
            vehErr.setVisible(true);
        }
    }
    
    /********************************************************************
    ** Helper method adds the vehicle to the database if no errors occur.
    ********************************************************************/
    private void helpAdd(){
        veh = new Vehicle(regNum.getText(), colour.getText(), Integer.parseInt(cMile.getText()), lastService.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), MoTDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), warrantyStatus == 0, warExpiration);
        
        String sql = "INSERT INTO Vehicles (CustomerID, RegPlate, Colour, Mileage, LastService, MOTDate, underWarranty, WarExpiration, WarrID, TemplateID) VALUES (" 
           + customer + ", '" + veh.getRegNumber() + "', '" + veh.getColour() + "', " + veh.getCurrentMileage() + ", '" + veh.getLastServiceDate() + "', '" + veh.getMoTRenewalDate() + "', " + warrantyStatus + ", '" + veh.getWarrantyExpiration()
           + "', " + getWarrantyID() + ", '" + getTemplateID() + "');";
        db.update(sql);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("The new vehicle has been added to the database");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK){
            Stage stage = (Stage) add.getScene().getWindow();
            stage.close();
        }
    }
    
    /************************************************************************
    ** Method creates a new vehicle template and inserts it into the vehicle 
    ** template table in the database.
    ************************************************************************/
    private void addTemplate(VehicleTemplate temp){
        try {
            String sql = "INSERT INTO Templates(Type, Make, Model, EngineSize, FuelType) VALUES ('" + temp.getType() + "', '" + temp.getMake() + "', '" + temp.getModel() + "', '" + temp.getEngineSize() + "', '" + temp.getFuelType() + "');";
            db.update(sql);
            ResultSet result = db.query("SELECT ID FROM Templates WHERE Type = '" + temp.getType() + "' AND Make = '" + temp.getMake() + "' AND Model = '" + temp.getModel() + "' AND EngineSize = '" + temp.getEngineSize() + "' AND FuelType = '" + temp.getFuelType() + "';"); 
            setTemplateID(result.getInt("ID"));            
            Alert newTemp = new Alert(Alert.AlertType.CONFIRMATION);
            newTemp.setContentText("New template has been added to the database");
            newTemp.showAndWait();
        } catch (SQLException ex) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("ERROR");
            error.setContentText("The new template could not be added to the database. Please check you have filled out the form correctly.");
            error.showAndWait();
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /************************************************************************
    ** Method creates a new warranty company entry and inserts it into the 
    ** warranty table in the database.
    ************************************************************************/
    private void addWarranty(WarrantyCompany wc){
        try {
            String sql = "INSERT INTO Warranty(CName, Address, City, Country, Postcode) VALUES ('" + wc.getCompanyName() + "', '" + wc.getCompanyAddress() + "', '" + wc.getCity() + "', '" + wc.getCountry() + "', '" + wc.getPostcode() + "');";
            db.update(sql);
            ResultSet result = db.query("SELECT WID FROM Warranty WHERE CName = '" + wc.getCompanyName() + "' AND Address = '" + wc.getCompanyAddress() + "' AND City = '" + wc.getCity() + "' AND Country = '" + wc.getCountry() + "' AND Postcode = '" + wc.getPostcode() + "';");
            setWarrantyID(result.getInt("WID"));
            Alert newWarr = new Alert(Alert.AlertType.CONFIRMATION);
            newWarr.setContentText("New warranty company has been added to the database");
            newWarr.showAndWait();
        } catch (SQLException ex) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("ERROR");
            error.setContentText("The new warranty company could not be added to the database. Please check you have filled out the form correctly.");
            error.showAndWait();
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*************************************************************************************************
    ** Method checks that the required fields for the vehicle details section, are filled in the form.
    **************************************************************************************************/
    private boolean checkDetails() throws SQLException{
        boolean check = true;
        if(customers.getValue() == null){
            selectCustomer.setTextFill(Color.web("#f71919"));
        }
        if(regNum.getText().isEmpty()){
           regErr.setVisible(true); 
        } else{
            ResultSet result = db.query("SELECT RegPlate FROM Vehicles WHERE RegPlate = '" + regNum.getText() + "';");
            while(result.next()){
                if(result.getString("RegPlate").equals(regNum.getText())){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Registration number already exists.");
                    alert.showAndWait();
                    regErr.setVisible(true);
                }
            }
        }
        custErr.setVisible(customers.getValue() == null);
        typeErr.setVisible((vType.getValue() == null));
        makeErr.setVisible(make.getValue() == null);
        modelErr.setVisible(model.getValue() == null);
        engErr.setVisible(engSize.getValue() == null);
        fuelErr.setVisible(fType.getValue() == null);
        colErr.setVisible(colour.getText().isEmpty());
        motErr.setVisible(MoTDate.getValue() == null);
        lastErr.setVisible(lastService.getValue() == null);
        try{
            cmileErr.setVisible(cMile.getText().isEmpty() || Integer.parseInt(cMile.getText()) < 0);
            if(customers.getValue() == null || vType.getValue() == null || regNum.getText().isEmpty() || make.getValue() == null || model.getValue() == null || engSize.getValue() == null || fType.getValue() == null || colour.getText().isEmpty() || cMile.getText().isEmpty() || Integer.parseInt(cMile.getText()) < 0 || MoTDate.getValue() == null || lastService.getValue() == null){
                check = false;
            }
        } catch (NumberFormatException e){
            cmileErr.setVisible(true);
            check = false;
        }
        
        return check;
    }
    
    /*************************************************************************************************
    ** Method checks that the required fields for the warranty detail sections are filled in the form.
    **************************************************************************************************/
    private boolean checkWarranty(){
        boolean check = true;
        nameErr.setVisible(cName.getText().isEmpty());
        addErr.setVisible(add1.getText().isEmpty());
        cityErr.setVisible(city.getText().isEmpty());
        countryErr.setVisible(country.getText().isEmpty());
        pcErr.setVisible(postcode.getText().isEmpty());
        expErr.setVisible(expDate.getValue() == null);
        if(cName.getText().isEmpty() || add1.getText().isEmpty() || city.getText().isEmpty() || country.getText().isEmpty() || postcode.getText().isEmpty() || expDate.getValue() == null){            
            check = false;
        }
        return check;
    }
    
    /**********************************************************************
    ** Method allows warranty section to be edited if checkbox is selected.
    ** Or uneditable if checkbox is unselected.
    **********************************************************************/
    public void showWarranty(ActionEvent event){
        if(warranty.isSelected()){
            warrantyDets.setDisable(false);
            expDate.setDayCellFactory(beforeDate());
        } else{
            warrantyDets.setDisable(true);
        }        
    }

    /*****************************************************************
    ** Method sets the combo boxes when a type of vehicle is selected.
    ******************************************************************/
    public void vehicleListener() {
        type = vType.getValue().toString();
        ResultSet result = db.query("SELECT * FROM Templates WHERE Type = '" + type + "' ORDER BY Make ASC;");     
        make.setItems(FXCollections.observableArrayList(setMake(result)));
        model.setItems(FXCollections.observableArrayList(setModel(result)));
        engSize.setItems(FXCollections.observableArrayList(setEng(result)));
        fType.setItems(FXCollections.observableArrayList(setFuel(result)));
    }

    /**************************************************************************
    ** Method sets the combo boxes when a type of vehicle and make is selected
    **************************************************************************/
    public void makeListener() {
        if(vType.getValue() != null) {
            type = vType.getValue().toString();
            vmake = make.getValue().toString();
            ResultSet result = db.query("SELECT DISTINCT * FROM Templates WHERE Type = '" + type + "' AND Make = '" + vmake + "';");
            model.setItems(FXCollections.observableArrayList(setModel(result)));
            engSize.setItems(FXCollections.observableArrayList(setEng(result)));
            fType.setItems(FXCollections.observableArrayList(setFuel(result)));
        }
    }

    /*********************************************************************************
    ** Method sets the combo boxes when a type of vehicle, make and model is selected
    *********************************************************************************/
    public void modelListener() {
        type = vType.getValue().toString();
        vmake = make.getValue().toString();
        vmodel = model.getValue().toString();
        ResultSet result = db.query("SELECT DISTINCT * FROM Templates WHERE Type = '" + type + "' AND Make = '" + vmake + "' AND Model = '" + vmodel + "';");
        engSize.setItems(FXCollections.observableArrayList(setEng(result)));
        fType.setItems(FXCollections.observableArrayList(setFuel(result)));
    }

    /*********************************************************************************************
    ** Method sets the combo boxes when a type of vehicle, make, model and engine size is selected
    **********************************************************************************************/
    public void engineListener() {
        type = vType.getValue().toString();
        vmake = make.getValue().toString();
        vmodel = model.getValue().toString();
        eSize = engSize.getValue().toString();
        ResultSet result = db.query("SELECT * FROM Templates WHERE Type = '" + type + "' AND Make = '" + vmake + "' AND Model = '" + vmodel + "' AND EngineSize = '" + eSize + "';");
        fType.setItems(FXCollections.observableArrayList(setFuel(result)));
    }

    /*******************************************************************
    ** Method fills arraylist with makes to populate the make combo box
    ********************************************************************/
    private ArrayList<String> setMake(ResultSet result) {
        ArrayList<String> list = new ArrayList<>();
        try {
            while(result.next()) {
                if(!list.contains(result.getString("Make"))){
                    list.add(result.getString("Make"));
                }  
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return list;
    }

    /*******************************************************************
    ** Method fills arraylist with models to populate the model combo box
    ********************************************************************/
    private ArrayList<String> setModel(ResultSet result) {
        ArrayList<String> list = new ArrayList<>();
        try {
            while(result.next()) {
                list.add(result.getString("Model"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    /********************************************************************************
    ** Method fills arraylist with engine sizes to populate the engine size combo box
    ********************************************************************************/
    private ArrayList<String> setEng(ResultSet result) {
        ArrayList<String> list = new ArrayList<>();
        try {
            while (result.next()) {
                list.add(result.getString("EngineSize"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    /****************************************************************************
    ** Method fills arraylist with fuel types to populate the fuel type combo box
    ****************************************************************************/
    private ArrayList<String> setFuel(ResultSet result) {
        ArrayList<String> list = new ArrayList<>();
        try {
            while (result.next()) {
                list.add(result.getString("FuelType"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    /*******************************************************************
    ** Method sets the initial query to populate the relevant combo box.
    ********************************************************************/
    public ResultSet initialCombo(String boxField){
        ResultSet result = null;
        switch (boxField) {
            case "Make":
                result = db.query("SELECT DISTINCT Make FROM Templates ORDER BY Make ASC;");
                break;
            case "Model":
                result = db.query("SELECT DISTINCT Model FROM Templates ORDER BY Model ASC;");
                break;
            case "EngineSize":
                result = db.query("SELECT DISTINCT EngineSize FROM Templates ORDER BY EngineSize ASC;");
                break;
            case "FuelType":
                result = db.query("SELECT DISTINCT FuelType FROM Templates ORDER BY FuelType ASC;");
                break;
            default:
                break;
        }
        return result;
    }
    
    /**********************************************************************************************
    ** Method checks if the company name entered is in the template table in the database when the 
    ** enter key is pressed. If it is the remaining template fields are filled.
    ***********************************************************************************************/
    public void autoFillCompName(){
        try {
            ResultSet result = db.query("SELECT * FROM Warranty WHERE CName LIKE '" + cName.getText() + "%';");
            if(!result.next()){
                result.close();
                setWarrantyID(-1);
            }else{
                cName.setText(result.getString("CName"));
                add1.setText(result.getString("Address"));
                city.setText(result.getString("City"));
                country.setText(result.getString("Country"));
                postcode.setText(result.getString("Postcode"));
                setWarrantyID(result.getInt("WID"));
            }  
            
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**************************************************************************
    ** Method fills arraylist with customers to populate the customer combo box
    ***************************************************************************/
    private ArrayList<String> getCustomers(){
        ResultSet result = db.query("SELECT * FROM Customers");
        ArrayList<String> cList = new ArrayList<>();
        try {
            while(result.next()){
                Customers c = new Customers(result.getInt("CustomerID"), result.getString("Type"), result.getString("FirstName"), result.getString("Lastname"), result.getString("Address Line 1") + " " + result.getString("Address Line 2"), result.getString("Postcode"), result.getString("Email"), result.getString("PhoneNumber"));
                cList.add(c.getCustomerID() + ". " + c.getFirstname() + " " + c.getSurname() + ", " + c.getAddress() + ", " + c.getPostcode());
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cList;
    }
    
    /********************************************************************************************
    ** Method fills arraylist with warranty companies to populate the warranty company combo box
    ********************************************************************************************/
    private ArrayList<String> getCompanies(){
        ResultSet result = db.query("SELECT * FROM Warranty");
        ArrayList<String> cList = new ArrayList<>();
        try {
            while(result.next()){
                cList.add(result.getInt("WID") + ". " + result.getString("CName") + ", " + result.getString("Address") + ", " + result.getString("City") + ", " + result.getString("City") + ", " + result.getString("Country") + ", " + result.getString("Postcode"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cList;
    }
    
    /**
     * **********************************************************************
     ** Method sets the customer ID. Queries the database and send it to the
     * method setCustomerDetails.
    **************************************************************************
     * @throws java.sql.SQLException
     */
    public void setCustomerID() throws SQLException {
        String cust = customers.getValue().toString();
        String[] deets = cust.split("[\\p{Punct}\\s]+");
        customer = Integer.parseInt(deets[0]);
    }

    /**********************************************
    ** Setter method sets the vehicle template ID.
    **********************************************/
    private void setTemplateID(int id){
        template = id;
    }
    
    /**********************************************
    ** Getter method gets the vehicle template ID.
    **********************************************/
    private int getTemplateID(){
        return template;
    }
    
    /**************************************
    ** Setter method sets the warranty ID.
    ***************************************/
    public void setWarranty() throws SQLException {
        String company = companies.getValue().toString();
        String[] deets = company.split("[\\p{Punct}\\s]+");
        warrantyID = Integer.parseInt(deets[0]);
        ResultSet result = db.query("SELECT * FROM Warranty WHERE WID = " + warrantyID);
        cName.setText(result.getString("CName"));
        add1.setText(result.getString("Address"));
        city.setText(result.getString("City"));
        country.setText(result.getString("Country"));
        postcode.setText(result.getString("Postcode"));
        
    }
    
    /***************************
    ** Method sets warranty id.
    ***************************/
    private void setWarrantyID(int id){
        warrantyID = id;
    }
   
    /**************************************
    ** Getter method gets the warranty ID.
    ***************************************/
    private int getWarrantyID(){
        return warrantyID;
    }
}
