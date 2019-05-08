package bfst19;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;


class Instruction extends HBox {


    Instruction(double length, String direction, String road){
        super();
        this.setStyle("-fx-min-width: 260; -fx-padding: 10; -fx-spacing: 2");
        getChildren().addAll(addImage(direction), makeLengthText(length), makeDirectionPane(direction), makeRoadNameText(road));
    }

    public Pane makeLengthText(double length){
        int lengthRound = Calculator.round(length);
        Pane lengthPane = new Pane();
        Label lengthText = new Label();
        lengthText.setText((lengthRound) + "m then ");
        lengthPane.getChildren().add(lengthText);
        return lengthPane;
    }

    public Pane makeDirectionPane(String direction){
        Pane directionPane = new Pane();
        Label directionLabel = new Label();
        String directionText = makeDirectionText(direction);
        directionLabel.setText(directionText);
        directionPane.getChildren().add(directionLabel);
        return directionPane;

    }
    public String makeDirectionText(String direction){
        String dir ="";
        if(direction.equals("right")){
            dir= "turn "+direction;
        }else if(direction.equals("left")){
            dir= "turn "+direction;
        }else if(direction.equals("keep right")){
            dir= direction;
        }else if(direction.equals("keep left")) {
            dir = direction;
        }else if(direction.equals("u-turn")){
            dir = "make a u-turn";
        } else if (direction.equals("")){
            // should be changed
            dir= "drive straight";
        }
        return dir;
    }

    public Pane makeRoadNameText(String roadName){
        Pane roadNamePane = new Pane();
        Label roadNameText = new Label();
        if(!roadName.equals("")){
            roadNameText.setText(" onto "+roadName);
        }else{
            roadNameText.setText(roadName);
        }

        roadNamePane.getChildren().add(roadNameText);
        return roadNamePane;
    }

    public Pane addImage(String dir){

        ImageView imageView= new ImageView();
        Image image= new Image("/white.png", true);
        Pane pane = new Pane();

        if (dir.equals("right")){
            image= new Image("/right.png", true);
        } else if (dir.equals("left")){
            image= new Image("/left.png", true);
        } else if(dir.equals("keep right")){
            image= new Image("/slight right.png", true);
        }else if(dir.equals("keep left")){
            image= new Image("/slight left.png", true);
        } else if (dir.equals("u-turn")){
            image= new Image("/Uturn.png", true);
        } else if (dir.equals("")) {
            image = new Image("/straight.png", true);
        }

        imageView.setImage(image);

        pane.getChildren().add(imageView);
        return pane;

    }

}


