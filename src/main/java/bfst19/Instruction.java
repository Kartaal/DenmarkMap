package bfst19;


import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


class Instruction extends HBox {

    Instruction(double length, String direction, String road) {
        super();
        this.setStyle("-fx-min-width: 260; -fx-padding: 10; -fx-spacing: 2");
        getChildren().addAll(addImage(direction), makeLengthText(length), makeDirectionPane(direction), makeRoadNameText(road));
    }

    private Pane makeLengthText(double length) {
        int lengthRound = Calculator.round(length);
        Pane lengthPane = new Pane();
        Label lengthText = new Label();

        lengthText.setText((lengthRound) + "m then");
        lengthPane.getChildren().add(lengthText);

        return lengthPane;
    }

    private Pane makeDirectionPane(String direction) {
        Pane directionPane = new Pane();
        Label directionLabel = new Label();

        String directionText = makeDirectionText(direction);
        directionLabel.setText(directionText);
        directionPane.getChildren().add(directionLabel);

        return directionPane;
    }

    private String makeDirectionText(String direction) {
        String dir = "";

        switch (direction) {
            case "right":
                dir = "turn " + direction;
                break;
            case "left":
                dir = "turn " + direction;
                break;
            case "keep right":
                dir = direction;
                break;
            case "keep left":
                dir = direction;
                break;
            case "u-turn":
                dir = "make a u-turn";
                break;
            case "":
                dir = "drive straight";
                break;
        }
        return dir;
    }

    private Pane makeRoadNameText(String roadName) {
        Pane roadNamePane = new Pane();
        Label roadNameText = new Label();

        if (!roadName.equals("")) {
            roadNameText.setText("onto " + roadName);
        } else {
            roadNameText.setText(roadName);
        }

        roadNamePane.getChildren().add(roadNameText);

        return roadNamePane;
    }

    private Pane addImage(String dir) {

        ImageView imageView = new ImageView();
        Image image = new Image("/white.png", true);
        Pane pane = new Pane();

        switch (dir) {
            case "right":
                image = new Image("/right.png", true);
                break;
            case "left":
                image = new Image("/left.png", true);
                break;
            case "keep right":
                image = new Image("/slight right.png", true);
                break;
            case "keep left":
                image = new Image("/slight left.png", true);
                break;
            case "u-turn":
                image = new Image("/Uturn.png", true);
                break;
            case "":
                image = new Image("/straight.png", true);
                break;
        }

        imageView.setImage(image);
        pane.getChildren().add(imageView);

        return pane;
    }
}


