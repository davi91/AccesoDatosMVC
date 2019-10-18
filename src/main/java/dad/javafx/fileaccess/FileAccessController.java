package dad.javafx.fileaccess;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;


/**
 * Pestaña de acceso a fichero
 * Controlador
 * @author Alumno
 *
 */

public class FileAccessController {

	private FileAccessModel model = new FileAccessModel();
	private FileAccessView view = new FileAccessView();
	
	private File rutaActual;
	
	public FileAccessController() {
		
		
		// Aún no sabemos como se interactúa, de momento bidireccional
		model.rutaProperty().bindBidirectional(view.getRutaTxt().textProperty());
		
	    model.fileProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty());
	    
		// Por acción del botón, mostramos la lista de ficheros, luego es el root el que se modifica a partir del model, y el model a partir del evento
		view.getFileList().itemsProperty().bind(model.fileListProperty());
		
		// File List
	    model.fileProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty());
	    
	    // Bindeamos el nombre del fichero con el fichero seleccionado
	    view.getNombreFichTxt().textProperty().bind(
	    		Bindings
	    		.when(model.fileProperty().isNull())  // ¿No tenemos nada?
	    		.then(new SimpleStringProperty("")) 
	    		.otherwise(model.fileProperty().asString())
	    );
	    		
		// En este caso, puesto que son los botones los que nos indican el mostrar el contenido y el modificarlo, es un bindeo hacia el modelo
		view.getContentArea().textProperty().bind(model.contentProperty());
		
		// Los radio buttons, es fichero o es carpeta
		model.isFolderProperty().bind(view.getFolderBt().selectedProperty());
		model.isFileProperty().bind(view.getFichBt().selectedProperty());
		
		// Eventos de botones......TO DO
		view.getViewBt().setOnAction( e -> onFolderViewAction(e)); 
		view.getCreateBt().setOnAction( e -> onCreateAction(e));
		view.getMoveBt().setOnAction(e -> onMoveAction(e));
		view.getRemoveBt().setOnAction(e -> onRemoveAction(e));
		view.getContentBt().setOnAction( e -> onContentViewAction(e)); 
		view.getModBt().setOnAction(e -> onModifyAction(e));
		
		// Temporalmente lo hacemos aquí para realizar pruebas
		rutaActual = new File("D:\\Users\\Alumno\\AED\\AccesoDatosMVC\\PruebaFicheros");
		model.setRuta(rutaActual.toString());
		
	}
	

	private void onMoveAction(ActionEvent e) {
		// TODO
	}

	private void onModifyAction(ActionEvent e) {
		// TODO
	}

	private void onContentViewAction(ActionEvent e) {

		// Mostramos el contenido del fichero

	}

	private void onRemoveAction(ActionEvent e) {
		// TODO
	}

	private void onCreateAction(ActionEvent e) {
		
		// Creamos un ficnhero en la ruta actual
		
	}

	private void onFolderViewAction(ActionEvent e) {

		// Aquí montamos la lista de ficheros y directorios
		File[] myFiles = rutaActual.listFiles();
		
		model.getFileList().addAll(myFiles);
	}

	public FileAccessView getRootView() {
		return view;
	}
}
