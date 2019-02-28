
package bfst19.addressparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			//simpel
			"^ *(?<street>[ÆØÅA-Zæøåa-z\\- ]+?)?[ \\.,]*(?<house>[0-9]+)? *$",
			//full simpel
			"^ *(?<street>[ÉÜÆØÅA-Züéäÿëèé(),\\/;.\\-'æøåa-z \\.]+?)?[ \\.,]*(?<house>[0-9]+? ??[ÆØÅæøåa-zA-Z]?)?[ \\.,]*(?<floor>[0-9]\\.)?[ \\.,]*(?<side>th|tv|mf)?[ \\.,]*(?<postcode>[0-9]{4})?[ \\.,]*(?<city>[æøåÆØÅA-Za-z\\. ]+?)?[ \\.,]*$",
			//full advanced: postcode city
			"^[ \\.,]*(?<street>(([A-Za-zæøåÆØÅ ])|([A-ZÆØÅ]\\.))*)?[ \\.,]*(?<house>([0-9]* ?[a-z]?))?[ \\.,]*(?<floor>([1-9][0-9]?\\.?)|(st\\.)|ST\\.)?[ \\.,]*(?<side>th|tv|mf|TH|TV|MF|([0-9][0-9]?[0-9]?))?[ \\.,]*(?<postcode>[0-9]{4})?[ \\.,]*(?<city>[æøåÆØÅA-Za-z\\. ]+?)?[ \\.,]*$",
			//full advanced: city postcode
			"^[ \\.,]*(?<street>(([A-Za-zæøåÆØÅ ])|([A-ZÆØÅ]\\.))*)?[ \\.,]*(?<house>([0-9]* ?[a-z]?))?[ \\.,]*(?<floor>([1-9][0-9]?\\.?)|(st\\.)|ST\\.)?[ \\.,]*(?<side>th|tv|mf|TH|TV|MF|([0-9][0-9]?[0-9]?))?[ \\.,]*(?<city>[æøåÆØÅA-Za-z\\. ]+?)?[ \\.,]*(?<postcode>[0-9]{4})?[ \\\\.,]*$",
			//haveforening full forkortede måneder
			"^[ \\.,]*(?<street>(([A-Za-zæøåÆØÅ ])|([A-ZÆØÅ]\\.)|(af[ \\.,]*([0-9][0-9]?)?[ \\.,]*(jan|feb|mar|apr|maj|jun|jul|aug|sep|oct|nov|dec)?[ \\.,]*([0-9]{4})?[ \\.,]*))*)?[ \\.,]*(?<house>([0-9]* ?[a-z]?))?[ \\.,]*(?<floor>([1-9][0-9]?\\.?)|(st)|(ST)|(St))?[ \\.,]*(?<side>th|tv|mf|TH|TV|MF|Th|Tv|Mf|([0-9][0-9]?[0-9]?))?[ \\.,]*(?<postcode>[0-9]{4})?[ \\.,]*(?<city>[æøåÆØÅA-Za-z\\. ]+?)?[ \\.,]*$",
			//haveforening fucked fx. "10. Februar Vej 11 Christiansfeld" and for some reason it parses the previous adress on regex101.com but not in the tests
			"^[ \\.,]*(?<Street>[0-9][0-9]?\\. ?[a-zA-Z ]*)?[ \\.,]*(?<house>[0-9]{1,3} ?[a-z]?)?[ \\.,]*(?<postcode>[0-9]{4})?[ \\.,]*(?<city>[a-zA-Z]*)?[ \\.,]*$"
	};

	final static Pattern[] patterns =
			Arrays.stream(regex).map(Pattern::compile).toArray(Pattern[]::new);

	private static void tryExtract(Matcher m, String group, Consumer<String> c) {
		try {
			c.accept(m.group(group));
		} catch (IllegalArgumentException e) {
			// Uncle Bob is going to kill me... ignore
		}
	}

	public String getStreetFromString(String adress)throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String obs;

		while((obs = in.readLine())!=null){
		    
        }

		return adress;
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
