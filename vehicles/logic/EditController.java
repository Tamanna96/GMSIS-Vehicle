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
 * EditFXML Controller class
 *
 * @author Tamanna Rahman
 */
public class EditController implements Initializable {

    //Edit form components.
    @FXML private Button save;
    @FXML private Button cancel;
    @FXML private TextField regNum;
    @FXML private TextField make;
    @FXML private TextField engSize;
    @FXML private TextField fType;
    @FXML private TextField colour;
    @FXML private TextField cMile;
    @FXML private TextField model;
    @FXML private DatePicker lastService;
    @FXML private DatePicker MoTDate;
    
    //Error labels for form
    @FXML private Label edErr;
    @FXML private Label regErr;
    @FXML private Label colErr;
    @FXML private Label cmileErr;
    @FXML private Label motErr;

    private Database db;
    private String regNumber;
    private int customerID;
    private int tID;
    private String update;
    private String last;
    private int miles;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        model.setEditable(false);
        make.setEditable(false);
        engSize.setEditable(false);
        fType.setEditable(false);
    }    
    
    /***********************************************************************************
    ** Method sets customer ID, registration number initialises the use of the database.
    ***********************************************************************************/
    public void setCustomerNVehicle(String reg, int customer){
        regNumber = reg;
        customerID = customer;
        db = Database.getInstance();
        ResultSet result = db.query("SELECT Vehicles.*, Templates.* FROM Vehicles, Templates WHERE Vehicles.RegPlate = '" + regNumber + "' AND Vehicles.TemplateID = Templates.ID;");
        setDetails(result);
    }
    
    /**********************************************************
    ** Method sets the vehicle's details in the edit window.
    **********************************************************/
    public void setDetails(ResultSet result){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(result.getString("MOTDate"), formatter);
            
            regNum.setText(result.getString("RegPlate"));
            model.setText(result.getString("Model"));
            make.setText(result.getString("Make"));
            engSize.setText(result.getString("EngineSize"));
            fType.setText(result.getString("FuelType"));
            colour.setText(result.getString("Colour"));
            cMile.setText(result.getString("Mileage"));
            miles = Integer.parseInt(cMile.getText());
            MoTDate.setValue(localDate);
            if(result.getString("LastService").equals("N/A")){
                lastService.setValue(null);
            }else{
                localDate = LocalDate.parse(result.getString("LastService"), formatter);
                lastService.setValue(localDate);
            }
            setTemplateID(result.getInt("TemplateID"));
            MoTDate.setDayCellFactory(beforeDate());
            lastService.setDayCellFactory(afterDate());
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
    
    /***********************************************************
    ** Method updates the vehicle's details in the database. If 
    ** it cannot update the details the user will be alerted.
    ************************************************************/
    public void pressSave(ActionEvent event) throws SQLException{
        boolean check = checkForm();
        if(check){
            edErr.setVisible(false);
            String MOT = MoTDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if(lastService.getValue() == null){
                last = "N/A";
            }else{
                last = lastService.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            update = "UPDATE Vehicles SET RegPlate = '" + regNum.getText() 
                    + "', Colour ='" + colour.getText() 
                    + "', Mileage = " + cMile.getText() 
                    + ", MOTDate = '" + MOT
                    + "', LastService = '" + last
                    + "' WHERE CustomerID = " + customerID + " AND RegPlate = '" + regNumber + "';";        

            if(db.update(update)){
                db.update("UPDATE Bookings SET RegPlate = '" + regNum.getText() 
                        + "' WHERE RegPlate = '" + regNumber + "';"); 
                db.update("UPDATE SPCBooking SET VehicleID = '" + regNum.getText() 
                            + "' WHERE VehicleID = '" + regNumber + "';");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Vehicle Details Updated");
                alert.setContentText("The vehicle's details have been updated successfully.");
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.OK){
                    Stage stage = (Stage) save.getScene().getWindow();
                    stage.close();
                } 
            }else{
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Update Unsuccessful");
                error.setContentText("Please check if you have filled in the form correctly.");
                error.showAndWait();
            } 
        }else{
            edErr.setVisible(true);
        }               
    }
    
    /**********************************************************
    ** Method checks if all required fields are filled.
    **********************************************************/
    public boolean checkForm() throws SQLException{
        boolean check = true;
        if(!regNumber.equals(regNum.getText())){
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
        
        if(regNum.getText().isEmpty()){
            regErr.setVisible(true);
        }
        colErr.setVisible(colour.getText().isEmpty());
        motErr.setVisible(MoTDate.getValue() == null);
        try{
            cmileErr.setVisible(cMile.getText().isEmpty() || Integer.parseInt(cMile.getText()) < 0);
            if(regNum.getText().isEmpty() || colour.getText().isEmpty() || cMile.getText().isEmpty() || Integer.parseInt(cMile.getText()) < miles || MoTDate.getValue() == null){
                check = false;
            }
        } catch (NumberFormatException e){
            cmileErr.setVisible(true);
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
    ** Setter method for the vehicle template ID.
    *********************************************/
    public void setTemplateID(int id){
        tID = id;
    }
    
    /*********************************************
    ** Getter method for the vehicle template ID.
    *********************************************/
    public int getTemplateID(){
        return tID;
    }
}
