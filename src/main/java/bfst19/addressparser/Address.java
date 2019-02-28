
package bfst19.addressparser;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Address {
	private final String street, house, floor, side, postcode, city;

	private Address(String _street, String _house, String _floor, String _side, String _postcode, String _city) {
		street = _street;
		house = _house;
		floor = _floor;
		side = _side;
		postcode = _postcode;
		city = _city;
	}

	public String toString() {
		return street + " " + house + ", " + floor + " " + side + "\n" + postcode + " " + city;
	}

	public static class Builder {
		private String street = "Unknown", house, floor, side, postcode, city;
		public Builder street(String _street) { street = _street; return this; }
		public Builder house(String _house)   { house = _house;   return this; }
		public Builder floor(String _floor)   { floor = _floor;   return this; }
		public Builder side(String _side)     { side = _side;     return this; }
		public Builder postcode(String _postcode) { postcode = _postcode; return this; }
		public Builder city(String _city)     { city = _city;     return this; }
		public Address build() {
			return new Address(street, house, floor, side, postcode, city);
		}
	}

	public String street()   { return street; }
	public String house()    { return house; }
	public String floor()    { return floor; }
	public String side()     { return side; }
	public String postcode() { return postcode; }
	public String city()     { return city; }


	final static String[] regex = {
			"^ *(?<street>[A-Za-z ]+?) +(?<house>[0-9]+) *$",
			"^ *(?<street>[A-Za-z ]+?) +(?<house>[0-9]+)[ ,]+(?<postcode>[0-9]{4}) +(?<city>[æøåÆØÅA-Za-z ]+?) *$"
	};

	final static Pattern[] patterns =
			Arrays.stream(regex).map(Pattern::compile).toArray(Pattern[]::new);

	private static void tryExtract(Matcher m, String group, Consumer<String> c) {
		String pattern = ".*\\(\\?\\<("+group+")\\>.*";
		if (m.pattern().pattern().matches(pattern)) {
			c.accept(m.group(group));
		}
	}

	public static Address parse(String s) {
		Builder b = new Builder();
		for (Pattern pattern : patterns) {
			Matcher match = pattern.matcher(s);
			if (match.matches()) {
				tryExtract(match, "street", b::street);
				tryExtract(match, "house", b::house);
				tryExtract(match, "floor", b::floor);
				tryExtract(match, "side", b::side);
				tryExtract(match, "postcode", b::postcode);
				tryExtract(match, "city", b::city);
				return b.build();
			}
		}
		return b.build();
	}
}
