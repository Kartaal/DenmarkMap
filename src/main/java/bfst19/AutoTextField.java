package bfst19;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AutoTextField extends TextField {

	Controller controller;
	Model model;

	private ContextMenu addressDropDown;

	public AutoTextField() {
		super();

		addressDropDown = new ContextMenu();
		addressDropDown.setStyle("-fx-max-height: 400");

		//Skal bruge onAction istedet, men den skal ikek være i controller da den kun skal aflæse om man klikker enter når man er inde i tekstfieldet
		setOnAction(event -> parseSearch());
	}

	public void init(Controller controller) {
		this.setStyle("-fx-min-width: 300; -fx-min-height: 40");
		this.controller = controller;
		this.model = controller.getModel();
		//Making sure to clear the old observer which should no longer be a living object.
		model.clearFoundMatchesObservers();
		model.addFoundMatchesObserver(this::showResults);
	}

	public void parseSearch() {
		controller.parseSearchText(getText());
	}

	public void showResults() {
		addAddressesToDropDown();
		if (!addressDropDown.isShowing()) {
			addressDropDown.show(AutoTextField.this, Side.BOTTOM, 0, 0);
		}
	}

	//TODO: Add ScrollPane and limit height
	private void addAddressesToDropDown() {

		List<CustomMenuItem> menuItems = new LinkedList<>();
		ArrayList<Label> addressLabels = new ArrayList<>();
		Iterator<String[]> iterator = controller.getFoundMatchesIterator();
		if (iterator.hasNext()) {
			String[] firstMatch = iterator.next();
			//this means that the match is a complete address
			if (firstMatch.length == 8) {
				panAddress(Float.valueOf(firstMatch[0]), Float.valueOf(firstMatch[1]));
				return;
				//and the rest of the address is passed of to some other part of the UI.
			} else if (firstMatch.length == 4) {
				addressLabels.add(new Label(firstMatch[0] + " " + firstMatch[1] + " " + firstMatch[2] + " " + firstMatch[3]));
				while (iterator.hasNext()) {
					String[] match = iterator.next();
					Label labelAddress = new Label(match[0] + " " + match[1] + " " + match[2] + " " + match[3]);
					addressLabels.add(labelAddress);
				}
			} else {
				addressLabels.add(new Label(firstMatch[0] + " " + firstMatch[1] + " " + firstMatch[2]));
				while (iterator.hasNext()) {
					String[] match = iterator.next();
					Label labelAddress = new Label(match[0] + " " + match[1] + " " + match[2]);
					addressLabels.add(labelAddress);
				}
			}

			for (Label addressLabel : addressLabels) {
				CustomMenuItem item = new CustomMenuItem(addressLabel, true);
				menuItems.add(item);

				item.setOnAction((event) -> {
					setText(addressLabel.getText());
				});
			}
		}

		if (menuItems.size() == 0) {
			menuItems.add(new CustomMenuItem(new Label("No search result found"), true));
		}

		addressDropDown.getItems().clear();
		addressDropDown.getItems().addAll(menuItems);
	}

	public void clear() {
		addressDropDown.getItems().clear();
	}

	//this.getText() er måske mere korrekt at skrive da man siger at det er klassen extended metode istedet for bare at skrive (getText())
	//Skal ikke være i controller da teksten fra AutoTextField skal sendes ud af denne klasse...
	private void panAddress(float x, float y) {
		controller.panToPoint(x, y);
		controller.setUpInfoPanel(this.getText(), x, y);
	}
}
