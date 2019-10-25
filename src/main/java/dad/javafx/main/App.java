package dad.javafx.main;

import dad.javafx.fileaccess.FileAccessController;
import dad.javafx.randomaccess.RandomAccessController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application {
	
	private FileAccessController tab_controlAccess;
	
	private RandomAccessController tab_randomAccess; // UNUSED
	@SuppressWarnings("unused")
	private FileAccessController tab_xmlAccess; // UNUSED
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		TabPane tab = new TabPane();
		
		tab_controlAccess = new FileAccessController();
		Tab tControl = new Tab("Acceso a ficheros");
		tControl.setContent(tab_controlAccess.getRootView());
		

		tab_randomAccess = new RandomAccessController();
		Tab tRAccess = new Tab("Acceso aleatorio");
		tRAccess.setContent(tab_randomAccess.getRootView());
		
		Tab tXML = new Tab("Acceso XML");
		
		tab.getTabs().addAll(tControl, tRAccess, tXML);
		
		Scene scene = new Scene(tab, 800, 600);
		
		primaryStage.setTitle("Acceso a datos");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);

	}

}
