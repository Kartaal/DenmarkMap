package bfst19.osmdrawing;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import javafx.scene.shape.Line;

import java.io.IOException;

public class View {
	public View(Model model, Stage stage) throws IOException {
		//Finds the .fxml file.
		FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
		//The loader loads the scene found in the .fxml file
		Scene scene = loader.load();
        //The loader loads the controller found in the .fxml file
        Controller controller = loader.getController();
        stage.setScene(scene);
		stage.show();
		controller.init(model);
	}

}

