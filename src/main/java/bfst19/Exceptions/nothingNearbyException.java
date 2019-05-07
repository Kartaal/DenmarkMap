package bfst19.Exceptions;

import javafx.scene.control.Alert;

public class nothingNearbyException extends RuntimeException {
	//This exception should be thrown, when all KDTree.getNearestNeighbors return null after x checks.

	public nothingNearbyException() {
		//We create an alert to have more user-feedback.
		// This allows the user to know, that they did something wrong, with a JavaFX alert in the program.
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error in Nearest Neighbor Search");
		alert.setHeaderText(null);
		alert.setContentText("We searched around the area, but could not find anything!");

		alert.showAndWait();
	}

}
