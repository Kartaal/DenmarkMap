package bfst19.osmdrawing;

import javafx.collections.ObservableArray;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

//import java.awt.*;

public class Controller implements Initializable{
	private Model model;
	double x, y;
	private double factor,oldDeterminant,zoomLevel;

	//This only means that .fxml can use this field despite visibility
    @FXML
	private MapCanvas mapCanvas;
	@FXML
	private TextField textField;
	@FXML
	private ListView listView;
	@FXML
	private SVGPath scalebarMiddle;
	@FXML
	private SVGPath scalebarRight;
	@FXML
	private Text scaleText;

    @FXML
    private Button hamburger;


	public void init(Model model) {
		//TODO: figure out init methods
	    this.model = model;
		mapCanvas.init(model);
		listView.setItems(model.addresses);
		oldDeterminant=mapCanvas.getDeterminant();

	}

    @Override /* Called to initialize a controller after its root element has been completely processed.*/
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        hamburger.setOnAction(this::getMenu);
    }

    private void getMenu(ActionEvent event) {
        System.out.println(hamburger+"was clikced");
    }



    @FXML
	private void onKeyPressed(KeyEvent e) {
		switch (e.getCode()) {//e.getcode() gets the specific keycode for the pressed key
		    case T: //toggle so that the canvas only draws roads or similar draws everything by default
				mapCanvas.toggleNonRoads();
				mapCanvas.repaint();
				break;
			case C: //Toggle colorblind colorfile
				model.switchColorScheme();
				break;
			case ENTER:
				model.parseSearch(textField.getText());
				textField.clear();
				break;

		}
	}

	@FXML
	private void onScroll(ScrollEvent e) {
		System.out.println("OldDeterminant: "+ mapCanvas.getDeterminant());
		double determinant= mapCanvas.getDeterminant();
        //because scrollwheels/touchpads scroll by moving up and down the zoomfactor as calculated based on the distance moved by the "scroll"
        //The pow part is just about trial and error to find a good amount of zoom per "scroll"

        factor = Math.pow(1.01, e.getDeltaY());
		mapCanvas.zoom(factor, e.getX(), e.getY());


		if ( (determinant - oldDeterminant)>0){
			oldDeterminant= determinant;
			System.out.println("getDeterminant: "+ mapCanvas.getDeterminant());
			scaleText.setText("100km");
			//scalebarMiddle.setContent("M10,0 L100,0");
			//scalebarRight.setContent("M100,-10 L100,0");
		}else {
			scaleText.setText("10km");
			//scalebarMiddle.setContent("M10,0 L400,0");
			//scalebarRight.setContent("M400,-10 L400,0");
		}

	}

	@FXML
	private void onMouseDragged(MouseEvent e) {
	    //pans based on difference between mousePressed event and current mouse coords
		if (e.isPrimaryButtonDown()) mapCanvas.pan(e.getX() - x, e.getY() - y);
		x = e.getX();
		y = e.getY();
	}

	@FXML
	private void onMousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}

	private ListView getListView(){
		return listView;
	}
}

