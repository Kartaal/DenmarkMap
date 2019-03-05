
package bfst19.osmdrawing;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Model model = new Model(getParameters().getRaw());//Gets a string from command line arguments, this must be given for javaFX to run.
		View view = new View(model, stage);
	}
}
