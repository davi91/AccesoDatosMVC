package dad.javafx.fileaccess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


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
	    
		// Por acción del botón, mostramos la lista de ficheros, luego es el root el que se modifica a partir del model, y el model a partir del evento
		view.getFileList().itemsProperty().bind(model.fileListProperty());
		
		// File List
	    model.fileProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty());
	    view.getNombreFichTxt().textProperty().bindBidirectional(model.fileNameProperty());

		// En este caso, puesto que son los botones los que nos indican el mostrar el contenido y el modificarlo, es un bindeo hacia el modelo
		view.getContentArea().textProperty().bindBidirectional(model.contentProperty());
		
		view.getFolderBt().selectedProperty().bindBidirectional(model.isFolderProperty());
	    view.getFichBt().selectedProperty().bindBidirectional(model.isFileProperty());
		
		// Eventos de botones
		view.getFileList().getSelectionModel().selectedItemProperty().addListener((o, lv, nv) -> onFileSelectionChanged(nv));
		
		view.getViewBt().setOnAction( e -> onFolderViewAction(e)); 
		view.getCreateBt().setOnAction( e -> onCreateAction(e));
		view.getMoveBt().setOnAction(e -> onMoveAction(e));
		view.getRemoveBt().setOnAction(e -> onRemoveAction(e));
		view.getContentBt().setOnAction( e -> onContentViewAction(e)); 
		view.getModBt().setOnAction(e -> onModifyAction(e));
		
		model.rutaProperty().bind(view.getRutaTxt().textProperty());						
		
	}
	

	/**
	 * Cuando cambia un elemento de la lista
	 * @param nv El fichero que ha cambiado
	 */
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


	/** 
	 * Para mover un fichero, es importante tenerlo seleccionado en la lista
	 * y luego poner la ruta a donde se quiere mover.
	 * @param e Evento
 	 */
	private void onMoveAction(ActionEvent e) {
		
		// Para mover, lo hacemos a partir de la ruta establecida
		File file = model.getFile();
		
		if( file != null ) {
			File dest = new File(model.getFileName());
			file.renameTo(dest);
		
			refreshList();
		} else {
			sendError("Mover fichero", "No hay ningún fichero seleccionado");
		}
	}

	/**
	 * Se modifica el fichero
	 * @param e Evento
	 */
	private void onModifyAction(ActionEvent e) {
		
		File file = model.getFile();
		if( file == null || file.isDirectory() ) {
			sendError("Fichero", "No tiene ningún fichero seleccionado o ha seleccionado un directorio");
		}
		
		// Mostramos el contenido del fichero
		FileOutputStream f = null;
		OutputStreamWriter out = null;
		BufferedWriter writer = null;
		
		try {
			
			f = new FileOutputStream(file);
			out = new OutputStreamWriter(f, java.nio.charset.StandardCharsets.UTF_8);
			writer = new BufferedWriter(out);
			
			// Volcamos todo el contenido de nuevo al fichero
			writer.write(model.getContent()); 
			
		} catch (IOException e1) {
			sendError("Escritura fichero", "Error al escribir en el fichero");
			e1.printStackTrace();
			
		} finally {
			
			try {
				if( writer != null ) {
					writer.close();
				}
				
				if( out != null ) {
					out.close();
				}
				if( f != null ) {
					f.close();
				}
			} catch (IOException e1) {
				sendError("Escritura fichero", "Error al escribir en el fichero");
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Se quiere visualizar el contenido del fichero
	 * @param e Evento
	 */
	private void onContentViewAction(ActionEvent e) {

		File file = model.getFile();
		if( file == null || file.isDirectory() ) {
			sendError("Fichero", "No tiene ningún fichero seleccionado o ha seleccionado un directorio");
		}
		
		// Mostramos el contenido del fichero
		FileInputStream f = null;
		InputStreamReader in = null;
		BufferedReader reader = null;
		StringBuilder fileText = new StringBuilder();
		
		try {
			
			f = new FileInputStream(file);
			in = new InputStreamReader(f, java.nio.charset.StandardCharsets.UTF_8);
			reader = new BufferedReader(in);
			
			String line;
			while( (line = reader.readLine()) != null ) {
				fileText.append(line + "\n"); // El salto de linea lo tenemos que poner manualmente
			}
			
			model.setContent(fileText.toString());
			
		} catch (IOException e1) {
			sendError("Leer fichero", "Error al leer en el fichero");
			e1.printStackTrace();
			
		} finally {
			
			try {
				if( reader != null ) {
					reader.close();
				}
				
				if( in != null ) {
					in.close();
				}
				if( f != null ) {
					f.close();
				}
			} catch (IOException e1) {
				sendError("Leer fichero", "Error al leer en el fichero");
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Acción de eliminar el fichero o directorio
	 * Si el directorio no está vacío dará error
	 * @param e
	 */
	private void onRemoveAction(ActionEvent e) {
		
		File file = model.getFile();
		
		if( file.delete() )
			refreshList();
		else 
			sendError("Eliminar fichero", "No hay un fichero seleccionado y/o no se pudo eliminar el fichero");
	
	}

	/**
	 * Creamos un fichero nuevo <b>en la ruta actual</b>
	 * @param e Evento
	 */
	private void onCreateAction(ActionEvent e) {
		
		// Creamos un ficnhero en la ruta actual
		File file = new File(model.getRuta() + "/" + model.getFileName());
		
		try {
			
			if( model.isIsFolder() ) {
				file.mkdir();
			}
			
			else {
				file.createNewFile();
			}
			
			refreshList();
			
		} catch (IOException e1) {

			sendError("Crear fichero", "No se pudo crear el fichero");
		}
		
	}

	/**
	 * Para ver el contenido del directorio
	 * @param e
	 */
	private void onFolderViewAction(ActionEvent e) {
		
		File rutaActual = new File(model.getRuta());
		
		if( rutaActual == null || !rutaActual.isDirectory()) {
			sendError("Ruta", "La ruta introducida no es válida");
		}
		else {
			// Aquí montamos la lista de ficheros y directorios	
			model.getFileList().addAll( rutaActual.listFiles() );
		}
	}
	
	/**
	 * Cada vez que se realize alguna acción sobre los ficheros, deberíamos
	 * de llamar a esta función.
	 */
	private void refreshList() {
		
		// Priemro nos aseguramos de vaciar la lista
		File rutaActual = new File(model.getRuta());
		
		model.getFileList().clear();
		model.getFileList().addAll(rutaActual.listFiles());
	}
	
	/**
	 * Envia un mensaje de error al usuario
	 * @param header Que clase de error
	 * @param msg El mensaje a mostrar
	 */
	private void sendError( String header, String msg ) {
		
		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText(header);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	public FileAccessView getRootView() {
		return view;
	}
}
