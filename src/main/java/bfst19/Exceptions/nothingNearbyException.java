package bfst19.Exceptions;

import javafx.scene.control.Alert;

public class nothingNearbyException extends Throwable {
    //This exception should be thrown, when all KDTree.getNearestNeighbors return null after x checks.

    public nothingNearbyException() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error in Nearest Neighbor Search");
        alert.setHeaderText(null);
        alert.setContentText("We searched around the area, but could not find anything!");

        alert.showAndWait();
    }

}