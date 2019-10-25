package dad.javafx.randomaccess;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

public class RandomAccessController implements Initializable {

	// VIEW
	// -- Ponemos el root
	
	@FXML
	private GridPane view;
	
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
		// TODO Auto-generated method stub
		
	}

}
