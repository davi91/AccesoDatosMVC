package dad.javafx.randomaccess;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class InsertDialog extends Dialog<Residencia> {

	private ButtonType okButton, cancelButton;
	private TextField id, nombre, codUni, precio;
	private CheckBox comedor;
	private Button myBt;
	
	public InsertDialog() {
		
		// Los textos
		setTitle("Insertar residencia");
		setHeaderText("Rellenar datos de la residencia");
		
		// Un bonito icono
		ImageView fileIcon = new ImageView(getClass().getResource("/images/fileIcon.png").toString());
		fileIcon.setFitWidth(48.0f);
		fileIcon.setFitHeight(48.0f);
		setGraphic(fileIcon);
		
		// Los botones
		
		okButton = new ButtonType("Insertar", ButtonData.OK_DONE);
		cancelButton = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
		
		myBt = (Button) getDialogPane().lookupButton(ButtonType.OK);
		myBt.setDisable(true);
		getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
		
		// Ahora nuestra vista
		GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(5);
		
		Label idLbl = new Label("ID:");
		id = new TextField();
		id.setPromptText("ID");
		id.setPrefColumnCount(2);
		grid.addRow(0, idLbl , id);
		
		Label nombreLbl = new Label("Nombre:");
		nombre = new TextField();
		nombre.setPromptText("Nombre");
		grid.addRow(1,  nombreLbl, nombre);
		
		Label codUniLbl = new Label("Universidad:");
		codUni = new TextField();
		codUni.setPromptText("Código");
		grid.addRow(2,  codUniLbl, codUni);
		
		Label precioLbl = new Label("Precio:");
		precio = new TextField();
		precio.setPromptText("Precio");
		precio.setPrefColumnCount(3);
		grid.addRow(3,  precioLbl, precio);
		
		Label comedorLbl = new Label("Comedor");
		comedor = new CheckBox();
		grid.addRow(4,  comedorLbl, comedor);
		
		getDialogPane().setContent(grid);

		setResultConverter( bt -> onActionPerformed(bt) );
	}


	private boolean checkValidFields() {
		
		boolean check;
		
		check = id.getText().isEmpty();
		check |= nombre.getText().isEmpty();
		check |= codUni.getText().isEmpty();
		check |= precio.getText().isEmpty();
		 
		return !check && checkIsNumeric(id.getText(), precio.getText());
	}
	
	@SuppressWarnings("unused")
	private boolean checkIsNumeric(String id, String precio) {
		
		try {
			
			int idVal = Integer.parseInt(id);
			float precioVal = Float.parseFloat(precio);
			
			return true;
			
		} catch( NumberFormatException e) {
			
		}
		
		return false;
	}
	
	private Residencia onActionPerformed(ButtonType bt) {
		
		if( bt == okButton ) {
			
			if( !checkValidFields() ) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Campos incorrectos");
				alert.setContentText("Debe rellenar todos los campos, los campos\n id y precio deben ser valores númericos");
				alert.showAndWait();
			}
			
			// Hemos garantizado que tienen que ser números
			float precioVal = Float.parseFloat(precio.getText());
			int idVal = Integer.parseInt(id.getText());
			
			return new Residencia(idVal, nombre.getText(),
								  codUni.getText(), precioVal,
								  comedor.isSelected());	
			
		}
		
		return null;
	}
}
