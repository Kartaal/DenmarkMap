package bfst19.Exceptions;

import javafx.scene.control.Alert;

public class noPathFoundException extends Exception {
	//This exception should be thrown, when there is no path available

	public noPathFoundException() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error in Path Finding");
		alert.setHeaderText(null);
		alert.setContentText("We could not establish a path between the points chosen!");

		alert.showAndWait();
	}

}
