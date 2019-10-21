package dad.javafx.fileaccess;

import java.io.File;
import java.io.IOException;

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
		
	    
		// Por acción del botón, mostramos la lista de ficheros, luego es el root el que se modifica a partir del model, y el model a partir del evento
		view.getFileList().itemsProperty().bind(model.fileListProperty());
		
		// File List
	    model.fileProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty());
	    view.getNombreFichTxt().textProperty().bindBidirectional(model.fileNameProperty());

		// En este caso, puesto que son los botones los que nos indican el mostrar el contenido y el modificarlo, es un bindeo hacia el modelo
		view.getContentArea().textProperty().bind(model.contentProperty());
		
		view.getFolderBt().selectedProperty().bind(model.isFolderProperty());
	    view.getFichBt().selectedProperty().bind(model.isFileProperty());
		
		// Eventos de botones
		view.getFileList().getSelectionModel().selectedItemProperty().addListener((o, lv, nv) -> onFileSelectionChanged(nv));
		
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
	

	private void onFileSelectionChanged(File nv) {
		
		// Aquí tenemos que comprobar el fichero que se ha seleccionado
		if( nv != null ) {
			
			model.setFileName(nv.getName());
			model.setIsFile(nv.isFile());
			model.setIsFolder(nv.isDirectory());
		}
		
		else {
			model.setFileName("");
			model.setIsFile(false);
			model.setIsFolder(false);
		}
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
		
		File file = model.getFile();
	}

	private void onCreateAction(ActionEvent e) {
		
		// Creamos un ficnhero en la ruta actual
		File file = new File(model.getRuta() + "/" + model.getFileName());
		
		try {
			file.createNewFile();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
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
