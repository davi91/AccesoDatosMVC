package dad.javafx.randomaccess;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

/**
 * En esta pestaña consultamos todo lo referenciado a las residecias. Para ello
 * nos valemos de un fichero con extensión '*.dat' que nos permitirá consultar esos
 * datos. 
 * <br>
 * También podemos modificarle el precio a una residencia e insertar una residencia nueva, 
 * todo en vivo, de manera que cualquier cambio en alguno de estos casos se actualizará 
 * directamente en el fichero.
 * <br>
 * También se permite consultar una residencia por el ID, mediante un cuadro de texto, al
 * igual que al modificar el precio.
 * 
 * @author David Fernández Nieves
 * @version 1.0
 *
 */
public class RandomAccessController implements Initializable {
	
	private static final int LEN_RESIDENCIAS = 10;
	private static final int LEN_UNIVERSIDAD = 6;
	
	/**
	 * El tamaño total de cada campo de residencia
	 */
	private static final int LEN_RESIDENCIA_BYTES = 51;
	
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
	
	private StringProperty id = new SimpleStringProperty();
	
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
		
		id.bindBidirectional(resiID.textProperty());
		
		/*
		 * Como no podemos hacer un "triple binding", si queremos que se nos
		 * actualize el ID con elemento seleccionado al mismo tiempo que 
		 * perimitimos escribir un ID nosotros mismos le ponemos un listener
		 */
		resiTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> onResiIDChanged(nv));
		resiID.disableProperty().bind(resiListProperty.emptyProperty());
	
		/*
		 *  La consulta y modificación la vamos a hacer por el ID introducido por el usuario.
		 *  En caso de que no haya ID se usará el elemento seleccionado en la lista.
		 */
		consultaIDBt.disableProperty().bind(resiListProperty.emptyProperty().or(
				
					resiID.textProperty().isEmpty().and(
					resiTable.getSelectionModel().selectedItemProperty().isNull()
				)));
		
		modPrecioBt.disableProperty().bind(resiListProperty.emptyProperty().or(
				
				resiID.textProperty().isEmpty().and(
				resiTable.getSelectionModel().selectedItemProperty().isNull()
			)));
		
		// Establecemos la ruta
		ruta.bind( Bindings
							.when(residenciasFile.isNotNull())
							.then(residenciasFile.asString())
							.otherwise(new SimpleStringProperty("")
		));
		
		rutaTxt.textProperty().bind(ruta);
		
		// Ajustamos la tabla para ajustar los campos de la misma
		resiTable.itemsProperty().bind(resiListProperty);
		insertResiBt.setOnAction( evt -> insertarResidencia() );
		consultaBt.setOnAction( evt -> consultaResidencias() );
		consultaIDBt.setOnAction( evt -> consultarResidenciaID());
		modPrecioBt.setOnAction( evt -> modificarPrecioID());
	}
	
	
	/**
	 * Cada vez que se produce un cambio en la lista modificamos el ID 
	 * de residencia seleccionado
	 * @param nv La residencia seleccionada
	 */
	private void onResiIDChanged(Residencia nv) {
		id.set( String.valueOf(nv.getId()));
	}

	/** 
	 * Tenemosq ue comprobar si la reesidencia es válida, para
	 * ello lo comparamos con los datos de la tabla para ver
	 * si ya existe ese ID.
	 * 
	 * @param myResi Residencia
	 * @return Si no existe un ID igual al introducido
	 */
	private boolean checkIfResiValid(Residencia myResi) {
		
		for( Residencia r : resiListProperty.get()) {
			
			if( r.getId() == myResi.getId() ) {
				return false;
			}
		}
		return true;
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
			
			if( !checkIfResiValid(result.get())) {
				
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Residencia");
				alert.setHeaderText("Coincidencia por ID");
				alert.setContentText("La residencia introducida ya existe");
				alert.showAndWait();
				return;
			}
			
			// Tenemos que ser cautelosos, tenemos que ajustar bien los caracteres
			insertResidenciaTable(result.get());
			
			// Seleccionamos la nueva residencia, debemos acceder a la tabla
			resiTable.getSelectionModel().clearSelection();
			resiTable.getSelectionModel().select(result.get());	
		}
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
				nombre += " ";
			}
		} else {
			nombre = nombre.substring(0,LEN_RESIDENCIAS); // No más de 10 caracteres
		}
		
		if( codUni.length() < LEN_UNIVERSIDAD ) {
			
			for( int i = codUni.length(); i < LEN_UNIVERSIDAD; i++ ) {
				codUni += " ";
			}
		} else {
			codUni = codUni.substring(0,LEN_UNIVERSIDAD); // No más de 10 caracteres
		}
		
		resiListProperty.add(new Residencia(myResi.getId(), nombre, codUni, myResi.getPrecio(), myResi.isComedor()));
		
		// Actualizamos el fichero con la nueva residencia
		
		RandomAccessFile resiFile = null;
		
		try {
			resiFile = new RandomAccessFile( residenciasFile.get(), "rw");
		
			// Nos vamos al final del fichero
			resiFile.seek(resiFile.length());
			
			// Ahora escribimos los datos de la residencia
			resiFile.writeInt(myResi.getId());
			resiFile.writeChar(',');
			resiFile.writeChars(nombre);
			resiFile.writeChar(',');
			resiFile.writeChars(codUni);
			resiFile.writeChar(',');
			resiFile.writeFloat(myResi.getPrecio());
			resiFile.writeChar(',');
			resiFile.writeBoolean(myResi.isComedor());
			resiFile.writeChar(',');
			
		} catch( EOFException eof) {
			// Se ejecuta el finally de forma automática
		} catch (IOException e) {
			sendFileError(residenciasFile.getName());
			
		} finally {
			
			if( resiFile != null ) {
				
				try {
					resiFile.close();
					
				} catch (IOException e) {
					
					sendFileError(residenciasFile.getName());
				}
			}
		} 
		
	}
	
	/**
	 * A partir de este método vamos a permitir al usuario
	 * determinar que archivo "dat" quiere abrir ( a partir
	 * de un explorador ) para consultar las residencias
	 * 
	 */
	private void consultaResidencias() {
		
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
			
			if( residenciasFile.get() != null ) {
				// Limpiamos lo que tengamos
				resiListProperty.clear();
			}
			
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
				
				resiFile.readChar(); // ','
				
				// Leemos 10 caracteres
				for( int i = 0; i < LEN_RESIDENCIAS; i++ ) 
					nombre += resiFile.readChar();
				
				resiFile.readChar(); // ','
				
				// Leemos 6 caracteres
				for( int i = 0; i < LEN_UNIVERSIDAD; i++ ) {
					codUni += resiFile.readChar();
				}
				
				resiFile.readChar(); // ','

				precio = resiFile.readFloat();
				
				resiFile.readChar(); // ','
				
				comedor = resiFile.readBoolean();
				
				resiFile.readChar(); // ','
				resiListProperty.add(new Residencia(id, nombre, codUni, precio, comedor));
				
				nombre = codUni = "";
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
	
	
	/**
	 * De forma normal consultaríamos los datos de la residencia
	 * a partir de la tabla, pero como estamos en Acceso a Datos,
	 * lo haremos a partir del fichero.
	 */
	private void consultarResidenciaID() {
		
		// Primero comprobamos que haya un ID válido
		if( !isIDValid(id.get())) {
			
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("ID");
			alert.setHeaderText("ID no válido");
			alert.setContentText("Por favor, introduzca un ID válido");
			alert.showAndWait();
			return;
		}
		
		// Ahora comprobamos que el ID existe y poodemos mostrar información
		RandomAccessFile resiFile = null;
		
		try {
			resiFile = new RandomAccessFile(residenciasFile.get(), "r");
			
			int rId = Integer.parseInt(id.get()); // ya sabemos que es válido
			// Para acceder al ID de la residencia, usamos la siguiente expresión
			resiFile.seek(LEN_RESIDENCIA_BYTES*(rId-1));
			
			int id;
			float precio;
			boolean comedor;
			String nombre, codUni;
			
			nombre = codUni = "";
			
			id = resiFile.readInt();
			resiFile.readChar(); // ',';
			
			for( int i = 0; i < LEN_RESIDENCIAS; i++ ) 
				nombre += resiFile.readChar();
			
			resiFile.readChar(); // ',';
			
			for( int i = 0; i < LEN_UNIVERSIDAD; i++ ) 
				codUni += resiFile.readChar();
			
			resiFile.readChar(); // ',';
			
			precio = resiFile.readFloat();
			resiFile.readChar(); // ',';
			
			comedor = resiFile.readBoolean();
			
			// Mostramos la información de la residencia
			InfoDialog dialog = new InfoDialog(new Residencia(id, nombre, codUni, precio, comedor));
			dialog.showAndWait();
			
		} catch(EOFException eof ) {
			
			// En caso de que hayamos llegado al final del fichero es que no hemos encontrado la residencia
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("ID");
			alert.setHeaderText("ID no válido");
			alert.setContentText("No se ha encontrado el ID seleccionado");
			alert.showAndWait();
			
		} catch (IOException e) {
			sendFileError(residenciasFile.getName());
			
		} finally {
			
			if( resiFile != null ) {
				
				try {
					
					resiFile.close();
					
				} catch (IOException e) {
					sendFileError(residenciasFile.getName());
				}
			}
		}
	}
	
	/**
	 * Modificamos el precio directamente en le fichero
	 */
	private void modificarPrecioID() {
		
		// Primero comprobamos que haya un ID válido
		if( !isIDValid(id.get())) {
			
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("ID");
			alert.setHeaderText("ID no válido");
			alert.setContentText("Por favor, introduzca un ID válido");
			alert.showAndWait();
			return;
		}
		
		// Ahora comprobamos que el ID existe y poodemos mostrar información
		RandomAccessFile resiFile = null;
		
		try {
			
			resiFile = new RandomAccessFile(residenciasFile.get(), "rw");
			
			// El precio está en el lugar 42 contando en bytes desde el principio de la residencia
			final int locPrecioBytes = 42;
			
			int rId = Integer.parseInt(id.get()); // ya sabemos que es válido
			
			// Para acceder al ID de la residencia, usamos la siguiente expresión
			resiFile.seek(LEN_RESIDENCIA_BYTES*(rId-1));
			resiFile.skipBytes(locPrecioBytes); // Nos despalazmos al precio, desde donde está el puntero
			
			/*
			 *  Necesitamos los datos de la residencia para actualizar la tabla,
			 *  para ello dejamos el fichero como está y simplemente accedemos
			 *  a la tabla
			 */
			
			// Aprovechamos el EOFException para lanzara el error de ID no válido, aunque no sea en el fichero, así agilizamos el proceso
			Residencia resi = findResidenciaById(rId);
			if( resi == null ) {
				throw new EOFException(); 
			}
			
			TextInputDialog dialog = new TextInputDialog(String.valueOf(resi.getPrecio()));
			dialog.setTitle("Residencia " + resi.getName());
			dialog.setHeaderText("Modificar precio");
			
			Optional<String> precio = dialog.showAndWait();
			float precioFinal = 0;
			
			// Tenemos un precio
			if( precio.isPresent() && !precio.get().isBlank() && !precio.get().isEmpty() ) {
				
				// Verificamos que el precio es válido
				try {
					precioFinal = Float.parseFloat(precio.get());
					
				} catch( NumberFormatException e) {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Precio");
					alert.setHeaderText("Precio no válido");
					alert.setContentText("El precio introducido no es válido");
					alert.showAndWait();
					throw new Exception(); // Nos aseguramos de cerrar el fichero entonces
				}
			} else {
				throw new Exception(); // Nos aseguramos de cerrar el fichero entonces
			}
			
			// Los datos a introducir son válidos, entonces procedemos
			
			// Primero actualizamos el fichero
			resiFile.writeFloat(precioFinal);
			
			// Ahora actualizamos la tabla, para no tener que cargar de nuevo todos los datos
			resi.setPrecio(precioFinal);
			
		} catch(EOFException eof ) {
			
			// En caso de que hayamos llegado al final del fichero es que no hemos encontrado la residencia
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("ID");
			alert.setHeaderText("ID no válido");
			alert.setContentText("No se ha encontrado el ID seleccionado");
			alert.showAndWait();
			
		} catch (IOException e) {
			sendFileError(residenciasFile.getName());
		} catch( Exception e) {
			// Nos sirve para poder cerrar el fichero al final
		} finally {
			
			if( resiFile != null ) {
				
				try {
					resiFile.close();
					
				} catch (IOException e) {
					sendFileError(residenciasFile.getName());
				}
			}
		}
	}
	
	/**
	 * Buscamos la residencia por el ID
	 * @param id El ID de la residencia
	 * @return La residencia coincidente con el ID o null si no se encuentra
	 */
	private Residencia findResidenciaById(int id) {
		
		ArrayList<Residencia> rList = new ArrayList<>(resiListProperty.get());
		
		try {
			return rList.stream().filter( r -> r.getId() == id).findFirst().get();
		} catch(NoSuchElementException e) {
			return null;
		}
	}
	
	/**
	 * Comprobamos que el ID es un número
	 * @param id El ID de la residencia
	 * @return Si el ID es un número o no
	 */
	private boolean isIDValid(String id) {
		
		try {
			
			@SuppressWarnings("unused")
			int idN = Integer.parseInt(id);
			return true; // Si no se ha producido una excepción
			
		} catch(NumberFormatException e) {
			// return false
		}
		
		return false;
	}
	
	private void sendFileError(String fileName) {
		
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error fichero");
		alert.setHeaderText("Error al cargar el fichero " + fileName);
		alert.showAndWait();
	}

	public final ListProperty<Residencia> resiListPropertyProperty() {
		return this.resiListProperty;
	}
	

	public final ObservableList<Residencia> getResiListProperty() {
		return this.resiListPropertyProperty().get();
	}
	

	public final void setResiListProperty(final ObservableList<Residencia> resiListProperty) {
		this.resiListPropertyProperty().set(resiListProperty);
	}
	

}
