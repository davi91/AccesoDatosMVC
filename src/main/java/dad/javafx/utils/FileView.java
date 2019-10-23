package dad.javafx.utils;

import java.io.File;

public class FileView extends File {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FileView(File f) {
		super(f.toString());
	}
	
	public FileView(String pathname) {
		super(pathname);
	}	

	/**
	 * Cambio con respecto al fichero original, para que 
	 * se nos muestre el nombre del fichero en lugar
	 * de la ruta completa
	 */
	@Override
	public String toString() {
		return this.getName();
	}

}
