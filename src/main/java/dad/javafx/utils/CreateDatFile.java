package dad.javafx.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Clase de apoyo para crearnos un fichero dat en caso de que no
 * tengamos ningún fichero .dat, con una residencia simple de ejemplo,
 * para salir del paso
 * @author David Fernández Nieves
 *
 */
public class CreateDatFile {

	public static void main(String[] args) {
		CreateDatFile datFile = new CreateDatFile();
	}
	public CreateDatFile() {
		
		RandomAccessFile file = null;
		
		try {
			file = new RandomAccessFile("miFichero.dat", "rw");
			
			file.setLength(0);
			// Escribimos uno de jemplo
			file.writeInt(1);
			
			file.writeChars("resiNueva ");
			
			file.writeChars("000001");
			
			file.writeFloat(1200);
			file.writeBoolean(true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				if( file != null ) {
					file.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}