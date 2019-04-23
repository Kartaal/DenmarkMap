
package bfst19.addressparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AddressBook extends Application {
	Model model = new Model();

	@Override
	public void start(Stage primaryStage) throws Exception {
		populateStage(primaryStage);
	}

	private void populateStage(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		TextField input = new TextField();
		ListView<Address> output = new ListView<>(model.addresses);
		input.setFont(Font.font(30));
		input.setOnAction(e -> {
			model.add(input.getText());
			input.clear();
		});
		pane.setTop(input);
		pane.setCenter(output);
		primaryStage.setScene(new Scene(pane));
		primaryStage.show();
	}
}
