package dad.javafx.fileaccess;

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
	
	public FileAccessController() {
		
		// Aún no sabemos como se interactúa, de momento bidireccional
		model.rutaProperty().bindBidirectional(view.getRutaTxt().textProperty());
		model.fileProperty().bindBidirectional(view.getNombreFichTxt().textProperty());
		
		// Por acción del botón, mostramos la lista de ficheros, luego es el root el que se modifica a partir del model, y el model a partir del evento
		view.getFileList().itemsProperty().bind(model.fileListProperty());
		
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
		
	}
	
	private void onMoveAction(ActionEvent e) {
		// TODO
	}

	private void onModifyAction(ActionEvent e) {
		// TODO
	}

	private void onContentViewAction(ActionEvent e) {
		// TODO
	}

	private void onRemoveAction(ActionEvent e) {
		// TODO
	}

	private void onCreateAction(ActionEvent e) {
		// TODO
	}

	private void onFolderViewAction(ActionEvent e) {
		// TODO
	}

	public FileAccessView getRootView() {
		return view;
	}
}
