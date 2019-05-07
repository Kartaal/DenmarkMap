package bfst19;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class View {
	public View(Model model, Stage stage) throws IOException {
		//Finds the .fxml file.
		FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));

		//The loader loads the scene found in the .fxml file
		Scene scene = loader.load();
        //The loader loads the controller found in the .fxml file
        Controller controller = loader.getController();
		controller.init(model);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(scene);
		stage.show();
	}

}

