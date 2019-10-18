package dad.javafx.fileaccess;

import java.io.File;
import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Pestaña de acceso a fichero
 * Modelo
 * @author Alumno
 *
 */

public class FileAccessModel {

	private StringProperty ruta = new SimpleStringProperty();
	private ObjectProperty<File> file = new SimpleObjectProperty<>();
	private StringProperty content = new SimpleStringProperty();
	
	// Esta parte es necesaria si queremos añadir elementos al array. El observableList tiene las funciones básicas para añadir elementos
	private ObservableList<File> observableList = FXCollections.observableArrayList(new ArrayList<File>());
	private ListProperty<File> fileList = new SimpleListProperty<>(observableList);
	
	private BooleanProperty isFolder= new SimpleBooleanProperty();
	private BooleanProperty isFile = new SimpleBooleanProperty();
	
	public final StringProperty rutaProperty() {
		return this.ruta;
	}
	
	public final String getRuta() {
		return this.rutaProperty().get();
	}
	
	public final void setRuta(final String ruta) {
		this.rutaProperty().set(ruta);
	}
	
	public final StringProperty contentProperty() {
		return this.content;
	}
	
	public final String getContent() {
		return this.contentProperty().get();
	}
	
	public final void setContent(final String content) {
		this.contentProperty().set(content);
	}
	
	public final ListProperty<File> fileListProperty() {
		return this.fileList;
	}
	
	public final ObservableList<File> getFileList() {
		return this.fileListProperty().get();
	}
	
	public final void setFileList(final ObservableList<File> fileList) {
		this.fileListProperty().set(fileList);
	}

	public final BooleanProperty isFolderProperty() {
		return this.isFolder;
	}
	

	public final boolean isIsFolder() {
		return this.isFolderProperty().get();
	}
	

	public final void setIsFolder(final boolean isFolder) {
		this.isFolderProperty().set(isFolder);
	}
	

	public final BooleanProperty isFileProperty() {
		return this.isFile;
	}
	

	public final boolean isIsFile() {
		return this.isFileProperty().get();
	}
	

	public final void setIsFile(final boolean isFile) {
		this.isFileProperty().set(isFile);
	}

	public final ObjectProperty<File> fileProperty() {
		return this.file;
	}
	

	public final File getFile() {
		return this.fileProperty().get();
	}
	

	public final void setFile(final File file) {
		this.fileProperty().set(file);
	}	
	
}
