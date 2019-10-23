package dad.javafx.fileaccess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import dad.javafx.utils.FileView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;


/**
 * Controlador
 * Pestaña de acceso a fichero.
 * Principalmente nos muestra una lista de ficheros y directorios contenidos en la ruta seleccionada.
 * Nos permite crear un fichero en la ruta seleccionada, moverlo a otra ruta, copiarlo y eliminarlo.
 * A la hora de crear un fichero <b><u>es importante seleccionar la opción correspondiente, si es fichero
 * o carpeta </u></b>.
 * También dado un fichero seleccionado nos permite ver su contenido y modificarlo si se quiere.
 * 
 * @author David Fernández Nieves
 * @version 1.0
 *
 */

public class FileAccessController {

	private FileAccessModel model = new FileAccessModel();
	private FileAccessView view = new FileAccessView();
	
	public FileAccessController() {
		
		// La ruta es establecida por el usuario
		model.rutaProperty().bindBidirectional(view.getRutaTxt().textProperty());	
	    
		// Por acción del botón, mostramos la lista de ficheros.
		view.getFileList().itemsProperty().bind(model.fileListProperty());
		
		// File List
		// -- Vinculamos la lista del modelo y la vista, modificando el fichero del modelo con el seleccionado
	    model.fileProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty());
	    
	    // Ajustamos el nombre del fichero
	    view.getNombreFichTxt().textProperty().bind(
	    		Bindings.when(model.fileProperty().isNull())
	    		.then(new SimpleStringProperty(""))
	    		.otherwise(model.fileProperty().asString())
	    );
	    
	    //  -- Desactivamos los botones de eliminar, mover y copiar si no tiene sentido activarlos
	    view.getRemoveBt().disableProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty().isNull());
	    view.getMoveBt().disableProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty().isNull());
	    view.getCopyBt().disableProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty().isNull());
	    
	    // -- Lo mismo para modificar/ver su contenido, sobretodo si no es un fichero
	    view.getModBt().disableProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty().isNull().or(
	    		model.isFolderProperty()));
	    
	    view.getContentBt().disableProperty().bind(view.getFileList().getSelectionModel().selectedItemProperty().isNull().or(
	    		model.isFolderProperty()));
	    
		// En este caso, puesto que son los botones los que nos indican el mostrar el contenido y el modificarlo, es un bindeo hacia el modelo
		view.getContentArea().textProperty().bindBidirectional(model.contentProperty());
		
		// Por defecto, en el modelo está seleccionado el fichero
		view.getFolderBt().selectedProperty().bindBidirectional(model.isFolderProperty());
	    view.getFichBt().selectedProperty().bindBidirectional(model.isFileProperty());
		
		// Eventos de botones
		view.getFileList().getSelectionModel().selectedItemProperty().addListener((o, lv, nv) -> onFileSelectionChanged(nv));
		view.getViewBt().setOnAction( e -> onFolderViewAction(e)); 
		view.getCreateBt().setOnAction( e -> onCreateAction(e));
		view.getMoveBt().setOnAction(e -> onMoveAction(e));
		view.getCopyBt().setOnAction(e -> onCopyAction(e));
		view.getRemoveBt().setOnAction(e -> onRemoveAction(e));
		view.getContentBt().setOnAction( e -> onContentViewAction(e)); 
		view.getModBt().setOnAction(e -> onModifyAction(e));
							
	}
	

	/**
	 * Opción para copiar un fichero/directorio a otra ruta.
	 * Si ya existe en la ruta objetivo otro fichero o directorio
	 * con el mismo nombre preguntamos si se quiere sobreescribir
	 * @param e
	 */
	private void onCopyAction(ActionEvent e) {
		
		File file = model.getFile();
		
		if( file != null ) {
			
			String nuevaRuta = sendDialog("Copiar", "Esablecer ruta", "Ruta para copiar el fichero/directorio " + file.getName());
			if( nuevaRuta.equals("")) {
				sendError("Copiar", "No ha establecido una ruta válida");
			}
			
			File dest = new File(nuevaRuta);
			// Si existe nos aseguramos antes de sobreescribir nada
			if( dest.exists() ) {
				boolean q = confirmDialog("Copiar", "Sobreescribir", "El fichero ya existe, \n¿Está seguro de sobreescribirlo?");
				
				if( q ) {
					
					try {
						Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						refreshList();
					} catch (IOException e1) {
						sendError("Copiar", "Error al copiar el fichero " + file.getName());
					}
				}
			}
			
			else {
				try {
					Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
					refreshList();
				} catch (IOException e1) {
					sendError("Copiar", "Error al copiar el fichero " + file.getName());
				}
			}
			
		}
	}


	/**
	 * Cuando cambia un elemento de la lista
	 * @param nv El fichero que ha cambiado
	 */
	private void onFileSelectionChanged(File nv) {
		
		// Aquí tenemos que comprobar el fichero que se ha seleccionado
		if( nv != null ) {
			model.setIsFile(nv.isFile());
			model.setIsFolder(nv.isDirectory());
		} else {
			model.setIsFile(false);
			model.setIsFolder(false);
		}
	}


	/** 
	 * Para mover un fichero, es importante tenerlo seleccionado en la lista
	 * y luego poner la ruta a donde se quiere mover, para eso mostramos
	 * un cuadro de diálogo que nos permita pedirle al usuario donde quiere
	 * el nuevo fichero( con su nuevo nombre si se especifica ).
	 * @param e Evento
 	 */
	private void onMoveAction(ActionEvent e) {
		
		// Para mover, lo hacemos a partir de la ruta establecida
		File file = model.getFile();
		
		if( file != null ) {
			
			String str;
			if( file.isDirectory() ) {
				str = sendDialog("Mover", "Mover directorio", "Establezca la nueva ubicación para el directorio\n "
						+ "junto con su nombre (puede ser un nuevo nombre) " + file.getName());
			} else {
				str = sendDialog("Mover", "Mover fichero", "Establezca la nueva ubicación para el fichero\n junto con"
						+ "su nombre (puede ser un nuevo nombre) " + file.getName()); 
			}
			
			if( str.equals("")) {
				sendError("Mover fichero/directorio", "No se ha establecido una ruta para el fichero/directorio");
				
			} else {
				
				File dest = new File(str);
				file.renameTo(dest);				
			}
		
			refreshList();
			
		} else {
			sendError("Mover fichero/directorio", "No hay ningún fichero/directorio seleccionado");
		}
	}

	/**
	 * Se modifica el contenido del fichero. Formato UTF-8
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
	 * Se quiere visualizar el contenido del fichero. Formato UTF-8
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
	 * Si el directorio no está vacío se eliminará
	 * todo su contenido, incluidos subdirectorios.
	 * @param e
	 */
	private void onRemoveAction(ActionEvent e) {
		
		File file = model.getFile();
		
		if( file.isDirectory() ) {
			boolean q = confirmDialog("Eliminar", "Eliminar directorio", "¿Está seguro de eliminar el directorio y su contenido?");
			if( q ) {
				
				// Al ser un directorio, eliminamos de forma recursiva
				removeDir(file);
				if( !file.delete() ) { // Ahora eliminamos el directorio que nos falta
					sendError("Eliminar fichero", "Se ha producido un error al borrar el directorio " + file.getName());
				}
				
				refreshList();
			} 
		}
		
		else {
			
			boolean q = confirmDialog("Eliminar", "Eliminar fichero", "¿Está seguro de eliminar este fichero?");
			if( q ) {
				if( file.delete() )
					refreshList();
				else 
					sendError("Eliminar fichero", "Ha habido un error al eliminar el fichero seleccionado");
			}
		}
	}

	private void removeDir(File file) {
		
		for( File f : file.listFiles()  ) {
			
			// Recursividad, si el fichero seleccionado es un directorio, eliminamos lo que contenga
			if( f.isDirectory() ) {
				removeDir(f);
			}
			
			// Eliminamos el fichero/directorio
			if( !f.delete() ) {
				sendError("Eliminar fichero", "Se ha producido un error al borrar el fichero/directorio " + file.getName());
			}
		}
	}
	
	/**
	 * Creamos un fichero nuevo <b>en la ruta actual</b>
	 * @param e Evento
	 */
	private void onCreateAction(ActionEvent e) {
		
		// Creamos un ficnhero en la ruta actual
		try {
			
			if( model.isIsFolder() ) {
				
				// Antes de nada abrimos un cuadro de diálogo para que nos pida el nombre del directorio
				String fileName = sendDialog("Crear directorio", "Nombre del directorio", "Introduzca el nombre del directorio a crear");
				if( fileName.equals("")) {
					sendError("Crear fichero", "No ha introducido un nombre correcto para el directorio");
					return;
				}
				
				File file = new File(model.getRuta() + "/" + fileName);
				file.mkdir();
			}
			
			else {
				// Antes de nada abrimos un cuadro de diálogo para que nos pida el nombre del fichero
				String fileName = sendDialog("Crear fichero", "Nombre del fichero", "Introduzca el nombre del fichero  a crear");
				if( fileName.equals("")) {
					sendError("Crear fichero", "No ha introducido un nombre correcto para el fichero");
					return;
				}
				
				File file = new File(model.getRuta() + "/" + fileName);
				file.createNewFile();
			}
			
			refreshList();
			
		} catch (IOException e1) {

			sendError("Crear fichero/directorio", "No se pudo crear el fichero/directorio, asegúrese de que la ruta es correcta");
		}
		
	}

	/**
	 * Para ver el contenido del directorio
	 * @param e
	 */
	private void onFolderViewAction(ActionEvent e) {
		
		FileView rutaActual = new FileView(model.getRuta());
		
		if( rutaActual == null || !rutaActual.isDirectory()) {
			sendError("Ruta", "La ruta introducida no es válida"); 
		}
		else {
			refreshList();
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
		
		for( File f : rutaActual.listFiles()) {
			
			FileView fView = new FileView(f);
			model.getFileList().add(fView);
			
		}

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

	/**
	 * Envia un mensaje al usuario pidiendo un texto
	 * @param title Título de la ventana
	 * @param header Contexto
	 * @param msg Mensaje a mostrar
	 * @return El texto introducido por el usuario
	 */
	private String sendDialog( String title, String header, String msg ) {
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(msg);
		
		// El texto es opcional, ya que el usuario puede o no introducir algo en el campo de texto
		Optional<String> str = dialog.showAndWait();
		return (str.isPresent()) ? str.get() : "";
	}
	
	/**
	 * Un cuadro de diálogo para confirmación por parte del usuario
	 * @param title Título de la ventana
	 * @param header Contexto
	 * @param msg Mensaje a mostrar
	 * @return Si el usuario a seleccionado 'SI' O 'NO'
	 */
	private boolean confirmDialog( String title, String header, String msg ) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(msg);
		
		Optional<ButtonType> btSelected = alert.showAndWait();
		if( !btSelected.isPresent() ) {
			return false;
		}
		
		return (btSelected.get() == ButtonType.OK) ? true : false;
	}
	
	/**
	 * Obtención de la vista principal
	 * @return La vista principal
	 */
	public FileAccessView getRootView() {
		return view;
	}
}
