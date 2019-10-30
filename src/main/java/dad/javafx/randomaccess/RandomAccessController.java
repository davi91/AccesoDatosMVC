package dad.javafx.randomaccess;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

public class RandomAccessController implements Initializable {
	
	private static final int LEN_RESIDENCIAS = 10;
	private static final int LEN_UNIVERSIDAD = 6;
	
	// FXML : View
	//-------------------------------------------------------------------------
	
	@FXML
	private GridPane view;
	
	@FXML
	private TableView<Residencia> resiTable;
	
	@FXML
	private Button insertResiBt, consultaBt;
	
	@FXML
	private TextField rutaTxt, resiID;
	
	@FXML 
	private Button consultaIDBt, modPrecioBt;
	
	//-------------------------------------------------------------------------
	
	// Model
	//-------------------------------------------------------------------------
	
	// -- De por sí la residencia tiene properties por si se modifica algún campo
	private ObservableList<Residencia> resiList = FXCollections.observableArrayList( new ArrayList<Residencia>() );
	
	// Vinculamos una lista para poder activar/desactivar botones
	private ListProperty<Residencia> resiListProperty = new SimpleListProperty<Residencia>(resiList);
	
	// Una referencia al fichero que estamos usando, en caso de que cambie, debemos establecer una limpieza
	private ObjectProperty<File> residenciasFile = new SimpleObjectProperty<File>();
	
	private StringProperty ruta = new SimpleStringProperty();
	
	//-------------------------------------------------------------------------
	
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
		
		// Desactivamos los botones si no hay ninguna lista cargada
		insertResiBt.disableProperty().bind(resiListProperty.emptyProperty()); 
		consultaIDBt.disableProperty().bind(resiTable.getSelectionModel().selectedItemProperty().isNull());
		modPrecioBt.disableProperty().bind(resiTable.getSelectionModel().selectedItemProperty().isNull());
		
		// Establecemos la ruta
		ruta.bind( Bindings
							.when(residenciasFile.isNotNull())
							.then(residenciasFile.asString())
							.otherwise(new SimpleStringProperty("")
		));
		
		rutaTxt.textProperty().bind(ruta);
		
		// Ajustamos la tabla para ajustar los campos de la misma
		resiTable.setItems(resiList);
		insertResiBt.setOnAction( evt -> insertarResidencia() );
		consultaBt.setOnAction( evt -> consultaResidencias() );
	}
	
	
	/**
	 * Insertamos una residencia a partir de un cuadro de
	 * diálogo mostrado al usuario.
	 */
	private void insertarResidencia() {
		
		InsertDialog dialog = new InsertDialog();

		// Cogemos los datos del usuario
		Optional<Residencia> result = dialog.showAndWait();
		
		// Ahora es cuando añadimos datos a la tabla
		if( result.isPresent() ) {
			
			// Tenemos que ser cautelosos, tenemos que ajustar bien los caracteres
			insertResidenciaTable(result.get());
			
			// Seleccionamos la nueva residencia, debemos acceder a la tabla
			resiTable.getSelectionModel().clearSelection();
			resiTable.getSelectionModel().select(result.get());	
		}
		
		// Debemos de guardar los cambios en caso de insertar o modificar
	}
	
	/**
	 * Insertamos la residencia en la tabla teniendo en cuenta los
	 * caracteres que tiene que tener cada campo.
	 */
	private void insertResidenciaTable(Residencia myResi) {
		
		// El id, el precio y el comedor no cambian
		// El nombre y el código de la universidad tienen unos caracteres determinados
		String nombre = myResi.getName();
		String codUni = myResi.getCodUniversidad();
		
		if( nombre.length() < LEN_RESIDENCIAS ) {
			
			for( int i = nombre.length(); i < LEN_RESIDENCIAS; i++ ) {
				nombre += "";
			}
		} else {
			nombre = nombre.substring(0,LEN_RESIDENCIAS);
		}
		
		if( codUni.length() < LEN_UNIVERSIDAD ) {
			
			for( int i = codUni.length(); i < LEN_UNIVERSIDAD; i++ ) {
				codUni += "";
			}
		} else {
			codUni = codUni.substring(0,LEN_UNIVERSIDAD);
		}
	}
	
	/**
	 * A partir de este método vamos a permitir al usuario
	 * determinar que archivo "dat" quiere abrir ( a partir
	 * de un explorador ) para consultar las residencias
	 * 
	 */
	private void consultaResidencias() {
		
		if( residenciasFile != null ) {
			// Limpiamos lo que tengamos
			resiList.removeAll();
		}
		
		// Primero vamos a abrir el explorador de archivos
		FileChooser explorer = new FileChooser();
		explorer.setTitle("Explorador residencias");
		// Empezamos por el directorio del usuario
		explorer.setInitialDirectory( new File( System.getProperty("user.home") ));
		// Sólo importamos los .dat
		explorer.getExtensionFilters().add(new FileChooser.ExtensionFilter("data", "*.dat"));
		// Ahora obtenemos el archivo seleccionado, para ello hay que vincularlo con la ventana actual
		File datFile = explorer.showOpenDialog(view.getScene().getWindow());
		
		// Aquí empezamosa implementar los métodos de consulta
		if( datFile != null ) {
			residenciasFile.set(datFile);
			
			// Ahora es cuando empezamos a consultar el archivo
			listarResidencias();
		}
	}
	
	/**
	 * Método llamado desde consultaResidencias() siempre y cuando el 
	 * archivo sea válido. Aquí rellenamos la tabla con los datos del
	 * fichero.
	 */
	private void listarResidencias() {
		
		// Esto es una medida de seguridad, aunque sólo debe ser llamado por consultaResidencias()
		if( residenciasFile.get() == null ) {
			return;
		}
		
		RandomAccessFile resiFile = null;
		
		try {
			
			resiFile = new RandomAccessFile(residenciasFile.get(), "r");
			
			// Ahora empezamos a leer el fichero
			int id;
			String nombre = "";
			String codUni = "";
			float precio;
			boolean comedor;
			
			// Leemos hasta el final del fichero, que se lanzará el EOFException
			while( true ) {
				
				id = resiFile.readInt();
				
				// Leemos 10 caracteres
				for( int i = 0; i < LEN_RESIDENCIAS; i++ ) 
					nombre += resiFile.readChar();
				
				// Leemos 6 caracteres
				for( int i = 0; i < LEN_UNIVERSIDAD; i++ ) {
					codUni += resiFile.readChar();
				}
				
				precio = resiFile.readFloat();
				comedor = resiFile.readBoolean();
				
				resiList.add(new Residencia(id, nombre, codUni, precio, comedor));
			}
			
			
		} catch(EOFException eof) {
			// Hemos terminado de leer el fichero
			// "finally" se asegura de cerrar el fichero tanto si ha habido algún error como si no.
		}
		
		catch (IOException e) {
			sendFileError(residenciasFile.getName());
		} finally {
			
			try {
				
				if( resiFile != null ) {
					resiFile.close();
				}
				
			} catch (IOException e) {
				sendFileError(residenciasFile.getName());
			}
		}
	}
	
	private void sendFileError(String fileName) {
		
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error fichero");
		alert.setHeaderText("Error al cargar el fichero " + fileName);
		alert.showAndWait();
	}

}
