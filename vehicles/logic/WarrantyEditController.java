package gmsis.vehicles.logic;

import gmsis.database.Database;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Edit Warranty FXML Controller class
 *
 * @author Tamanna Rahman
 */
public class WarrantyEditController implements Initializable {

    @FXML private Button save;
    @FXML private Button cancel;
    @FXML private TextField cName;
    @FXML private TextField add1;
    @FXML private TextField city;
    @FXML private TextField country;
    @FXML private TextField postcode;
    @FXML private DatePicker expDate;
    
    @FXML private Label warErr;
    @FXML private Label nameErr;
    @FXML private Label addErr;
    @FXML private Label cityErr;
    @FXML private Label countryErr;
    @FXML private Label pcErr;
    @FXML private Label expErr;

    private Database db;
    private String regNumber;
    private int customerID;
    private int wID;
    private String update;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    /***********************************************************************************
    ** Method sets customer ID, registration number initialises the use of the database.
    ***********************************************************************************/
    public void setCustomerNVehicle(String reg, int customer){
        regNumber = reg;
        customerID = customer;
        db = Database.getInstance();
        ResultSet result = db.query("SELECT Vehicles.*, Warranty.* FROM Vehicles, Warranty WHERE Vehicles.RegPlate = '" + regNumber + "' AND Vehicles.CustomerID = " + customerID + " AND Vehicles.WarrID = Warranty.WID;");
        setDetails(result);
    }
    
    /**********************************************************
    ** Method sets the vehicle's details in the edit window.
    **********************************************************/
    public void setDetails(ResultSet result){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(result.getString("WarExpiration"), formatter);
            
            cName.setText(result.getString("CName"));
            add1.setText(result.getString("Address"));
            city.setText(result.getString("City"));
            country.setText(result.getString("Country"));
            postcode.setText(result.getString("Postcode"));
            expDate.setValue(localDate);
            setWarrantyID(result.getInt("WID"));
            expDate.setDayCellFactory(beforeDate());
        } catch (SQLException ex){
            Logger.getLogger(EditController.class.getName()).log(Level.SEVERE, null, ex);
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
    
    /***********************************************************
    ** Method updates the vehicle's details in the database. If 
    ** it cannot update the details the user will be alerted.
    ************************************************************/
    public void pressSave(ActionEvent event){
        boolean check = checkForm();
        if(check){
            warErr.setVisible(false);
            String exp = expDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            update = "UPDATE Vehicles SET WarExpiration = '" + exp
                    + "' WHERE CustomerID = " + customerID + " AND RegPlate = '" + regNumber + "';";        

            if(db.update(update)){
                update = "UPDATE Warranty SET CName = '" + cName.getText() 
                        + "', Address = '" + add1.getText()
                        + "', City = '" + city.getText()
                        + "', Country = '" + country.getText()
                        + "', Postcode = '" + postcode.getText()
                        + "' WHERE WID   = '" + wID + "';"; 
                if(db.update(update)){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("Warranty Details Updated");
                    alert.setContentText("The warranty details have been updated successfully.");
                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == ButtonType.OK){
                        Stage stage = (Stage) save.getScene().getWindow();
                        stage.close();
                    }
                }
            }else{
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Update Unsuccessful");
                error.setContentText("Please check if you have filled in the form correctly.");
                error.showAndWait();
            } 
        }else{
            warErr.setVisible(true);
        }               
    }
    
    /**********************************************************
    ** Method checks if all required fields are filled.
    **********************************************************/
    public boolean checkForm(){
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
    
    /*******************************
    ** Method closes the edit page.
    *******************************/
    public void pressCancel(ActionEvent event){
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }
    
    /*********************************************
    ** Getter method for the registration number.
    *********************************************/
    public String getRegPlate(){
        return regNumber;
    }
    
    /*********************************************
    ** Setter method for the vehicle warranty ID.
    *********************************************/
    public void setWarrantyID(int id){
        wID = id;
    }
    
    /*********************************************
    ** Getter method for the vehicle warranty ID.
    *********************************************/
    public int getWarrantyID(){
        return wID;
    }
    
}
