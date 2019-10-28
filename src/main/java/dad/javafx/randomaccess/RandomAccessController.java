package dad.javafx.randomaccess;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class RandomAccessController implements Initializable {

	// VIEW
	// -- Ponemos el root
	
	@FXML
	private GridPane view;
	
	@FXML
	private TableView<Residencia> resiTable;
	
	@FXML
	private Button insertResiBt;
	
	// Model
	// -- De por sí la residencia tiene properties por si se modifica algún campo
	private ObservableList<Residencia> resiList = FXCollections.observableArrayList( new ArrayList<Residencia>() );
	
	public RandomAccessController() throws IOException {
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RandomAccessView.fxml"));
		loader.setController(this);
		loader.load();
	}
	
	public GridPane getRootView() {
		return view;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// Ajustamos la tabla para ajustar los campos de la misma
		resiTable.setItems(resiList);
		insertResiBt.setOnAction( evt -> insertarResidencia() );
	}
	
	
	/**
	 * Insertamos una residencia a partir de un cuadro de
	 * diálogo mostrado al usuario.
	 */
	public void insertarResidencia() {
		
		InsertDialog dialog = new InsertDialog();
		
		// Cogemos los datos del usuario
		Optional<Residencia> result = dialog.showAndWait();
		
		// Ahora es cuando añadimos datos a la tabla
		if( result.isPresent() ) {
			resiList.add(result.get());
		}
		
	}

}
