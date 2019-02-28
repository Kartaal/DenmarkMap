package bfst19.addressparser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
	ObservableList<Address> addresses = FXCollections.observableArrayList();

	public void add(String s) {
		addresses.add(Address.parse(s));
	}
}
