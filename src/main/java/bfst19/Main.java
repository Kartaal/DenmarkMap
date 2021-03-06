package bfst19;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    Model model;

    @Override
    public void start(Stage stage) throws Exception {
        //Gets a string from command line arguments, this must be given for javaFX to run.
        model = new Model(getParameters().getRaw());
        View view = new View(model, stage);
    }

    @Override
    public void stop() {
        model.writePointsOfInterest();
        // Save file
    }
}
