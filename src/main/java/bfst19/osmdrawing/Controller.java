package bfst19.osmdrawing;

import javafx.collections.ObservableArray;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.MultipleSelectionModel;

//import java.awt.*;

public class Controller implements Initializable{
	private Model model;
	double x, y;
	private double factor,oldDeterminant,zoomLevel;
	private ArrayList<String> selectedAddresse;


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
    @FXML
    private Button ruteInstructions;
    @FXML
    private Button closeListPane;
    @FXML
    private Pane listPane;
    @FXML
    private Button closeTogglePane;
    @FXML
    private Pane togglePane;
    @FXML
    private ToggleButton toggleColorBlindMode;
    @FXML
    private Pane rutePane;
    @FXML
    private Button closeRutePane;
    @FXML
    private Label to;
    @FXML
    private Label from;


    public void init(Model model) {
		//TODO: figure out init methods
	    this.model = model;
		mapCanvas.init(model);
		listView.setItems(model.addresses);
		oldDeterminant=mapCanvas.getDeterminant();
		listPane.setVisible(false);
        togglePane.setVisible(false);
        rutePane.setVisible(false);
        // the initial scale
        setScalebar();

        mapCanvas.transform.mxxProperty().addListener((observable, oldVal, newVal)->{
            // sets the after mx is changed scale
            setScalebar();
        });

	}

    @Override /* Called to initialize a controller after its root element has been completely processed.*/
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        hamburger.setOnAction(this::getMenu);
        closeListPane.setOnAction(this::closeListPane);
        closeTogglePane.setOnAction(this::closeTogglePane);
        closeRutePane.setOnAction(this::closeRutePane);
        toggleColorBlindMode.setOnAction(this::toggleColorBlindMode);
        ruteInstructions.setOnAction(this::openRutePane);
    }

    private void getMenu(ActionEvent event) {
        togglePane.setVisible(true);
    }

    public void setScalebar(){
        double minX = mapCanvas.getModelCoords(0,0).getY();
        double maxX = mapCanvas.getModelCoords(0,mapCanvas.getHeight()).getY();
        double y = mapCanvas.getModelCoords(0,0).getX();
        scaleText.setText(Scalebar.getScaleText(minX,y,maxX,y,mapCanvas.getWidth()));
    }

    private void closeListPane(ActionEvent event) {
        listPane.setVisible(false);

    }
    private void closeTogglePane(ActionEvent event) {
        togglePane.setVisible(false);
    }

    private void closeRutePane(ActionEvent event) {
        rutePane.setVisible(false);
    }
    private void openRutePane(ActionEvent event) {
        rutePane.setVisible(true);
        getSelectedAddresses(event);
        setToAndFrom(event);
    }

    private void toggleColorBlindMode(ActionEvent event) {
        model.switchColorScheme();
    }

    private void setToAndFrom(ActionEvent event){
        to.setText("Til: sup" );
        from.setText("Fra: sup");
    }
    private void getSelectedAddresses(ActionEvent event){


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
                listPane.setVisible(true);

				textField.clear();
				break;

		}
	}

	@FXML
	private void onScroll(ScrollEvent e) {
        //because scrollwheels/touchpads scroll by moving up and down the zoomfactor as calculated based on the distance moved by the "scroll"
        //The pow part is just about trial and error to find a good amount of zoom per "scroll"

        factor = Math.pow(1.01, e.getDeltaY());
		mapCanvas.zoom(factor, e.getX(), e.getY());


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

