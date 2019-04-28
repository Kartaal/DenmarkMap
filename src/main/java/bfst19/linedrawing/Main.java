
package bfst19.linedrawing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		Model model = new Model(getParameters().getRaw());
		View view = new FancyView(model, stage);
		Controller controller = new Controller(model, view);
	}
}
