
package bfst19.osmdrawing;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser {
	private final String street, house, floor, side, postcode, city;

	private static ArrayList<String> cities = new ArrayList<>();
	private static ArrayList<String> postcodes = new ArrayList<>();

	private AddressParser(String _street, String _house, String _floor, String _side, String _postcode, String _city) {
		street = _street;
		house = _house;
		floor = _floor;
		side = _side;
		postcode = _postcode;
		city = _city;
	}

	public String toString() {
		return ""+street + " " + house + ", " + floor + " " + side + "\n" + postcode + " " + city;
	}

	public static class Builder {
		private String street = "Unknown", house, floor, side, postcode, city;
		public Builder street(String _street) { street = _street; return this; }
		public Builder house(String _house)   { house = _house;   return this; }
		public Builder floor(String _floor)   { floor = _floor;   return this; }
		public Builder side(String _side)     { side = _side;     return this; }
		public Builder postcode(String _postcode) { postcode = _postcode; return this; }
		public Builder city(String _city)     { city = _city;     return this; }
		public AddressParser build() {
			return new AddressParser(street, house, floor, side, postcode, city);
		}
	}

	public String street()   { return street; }
	public String house()    { return house; }
	public String floor()    { return floor; }
	public String side()     { return side; }
	public String postcode() { return postcode; }
	public String city()     { return city; }

	final static String houseRegex = "(?<house>([0-9]{1,3} ?[a-z]?))";
	final static String floorRegex = "(?<floor>([1-9][0-9]?\\.?)|(st\\.)|ST\\.)";
	final static String sideRegex = "(?<side>th|tv|mf|TH|TV|MF|([0-9][0-9]?[0-9]?))";

	final static String[] regex = {
			"^(?<house>([0-9]{1,3} ?[a-zA-Z]?))?,? ?(?<floor>([1-9]{1,3}\\.?)|(1st\\.)|(st\\.)?,? ?(?<side>th\\.?|tv\\.?|mf\\.?|md\\.?|([0-9]{1,3}\\.?))?,?$"
	};

	final static Pattern[] patterns =
			Arrays.stream(regex).map(Pattern::compile).toArray(Pattern[]::new);

	private static void tryExtract(Matcher m, String group, Consumer<String> c) {
		try {
			c.accept(m.group(group));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public static AddressParser parseAddressString(String proposedAddress){
		Builder b = new Builder();



		return b.build();
	}

	public static AddressParser parse(String address){
		Builder b = new Builder();
		try {
			address = address.toLowerCase().trim();
			//this finds a potential street for the address and if this is "", there is no match.
			String streetMatch = checkStreet(address, b);
			if (!streetMatch.equals("")) {
				//if a match is found, it is removed from the string.
				address = address.replace(streetMatch, "");
			}
			//This finds a potential city + postcode for the proposed address, if no match it return "".
			String[] cityMatch = CityCheck(address);
			if (!(cityMatch[0].equals(""))) {
				address = address.replace(cityMatch[0], "");
				b.postcode = cityMatch[1];
				b.city = cityMatch[2];
			}
			//check the remaining part of the proposed address against a regex pattern
			address = address.trim();

			for (Pattern pattern : patterns) {
				Matcher match = pattern.matcher(address);
				if (match.matches()) {
					tryExtract(match, "house", b::house);
					tryExtract(match, "floor", b::floor);
					tryExtract(match, "side", b::side);
					return b.build();
				}
			}
			return b.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b.build();
	}

	//Checks if start of the address matches any the streets in the street names file
	// if a match is found, the builders street field is set to the match
	// which is returned to be removed from the address.
	public static String checkStreet(String address, Builder b) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/streetnames.txt"),"UTF-8"));
		String line = in.readLine();
		String mostCompleteMatch = "";
		while(line != null){
			if(address.startsWith(line.toLowerCase())){
				if(line.length() > mostCompleteMatch.length()){
					mostCompleteMatch = line;
				}
			}
			line = in.readLine();
		}
		b.street = mostCompleteMatch;
		return mostCompleteMatch.toLowerCase();
	}

	public static String[] CityCheck(String proposedAddress) throws Exception{
		if(cities.isEmpty() && postcodes.isEmpty()){
			parseCitiesAndPostCodes();
		}
		String currentCity = "", currentPostcode = "", mostCompleteMatch = "",
				bestPostCodeMatch = "", bestCityMatch = "";

		for(int i = 0 ; i < cities.size() ; i++){
			currentCity = cities.get(i);
			currentPostcode = postcodes.get(i);

			//TODO these need rewriting badly, two methods that basically do the same thing??
			String checkSecondToLastToken = checkSecondToLastTokenForPostcode(proposedAddress, currentPostcode).toLowerCase();
			String checkThirdToLastToken = checkThirdToLastTokenForPostcode(proposedAddress, currentPostcode).toLowerCase();

			if(proposedAddress.endsWith(currentPostcode.toLowerCase() +" "+ currentCity.toLowerCase())){
				mostCompleteMatch = currentPostcode +" "+ currentCity;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}else if(!(checkSecondToLastToken.equals(""))){
				mostCompleteMatch = checkSecondToLastToken;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}else if(!(checkThirdToLastToken.equals(""))){
				mostCompleteMatch = checkThirdToLastToken;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}else if((proposedAddress.endsWith(currentPostcode.toLowerCase()))){
				mostCompleteMatch = currentPostcode;
				bestCityMatch = currentCity;
				bestPostCodeMatch = currentPostcode;
			}else if(proposedAddress.endsWith(currentCity.toLowerCase())){
				mostCompleteMatch = currentCity;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}

		}
		//if not "" it means it has found a match inside the loop
		if(!(mostCompleteMatch.equals(""))){
			return new String[]{mostCompleteMatch, bestCityMatch, bestPostCodeMatch};
		}

		return new String[]{""};
	}

	//if second to last token in the proposed address is the given postcode,
	// it returns the part of the address to remove, if not it returns "".
	public static String checkSecondToLastTokenForPostcode(String proposedAdress,String postcode){
		String[] addressTokens = proposedAdress.split(" ");
		if(addressTokens.length>=2 && addressTokens[addressTokens.length-2].equals(postcode)) {
			return (addressTokens[addressTokens.length - 2] + " " + addressTokens[addressTokens.length - 1]);
		}
		return "";
	}

	//if third to last token in the proposed address is the given postcode,
	// it returns the part of the address to remove, if not it returns "".
	public static String checkThirdToLastTokenForPostcode(String proposedAdress,String postcode){
		String[] adressTokens = proposedAdress.split(" ");
		if(adressTokens.length>=3 && adressTokens[adressTokens.length-3].equals(postcode)) {
			return (adressTokens[adressTokens.length - 3] + " " + adressTokens[adressTokens.length - 2]
							+ " " + adressTokens[adressTokens.length-1]);
		}
		return "";
	}

	public static void parseCitiesAndPostCodes()throws Exception{

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/postnumre.txt"),"UTF-8"));
		//the first line of the file starts with a ?,
		// to combat this i've moved everything 1 line down, so now i skip the first line.
		String line = in.readLine();
		line = in.readLine().toLowerCase();

		while(line != null){
			String[] tokens = line.split(" ",2);
			postcodes.add(tokens[0]);
			cities.add(tokens[1]);
			line = in.readLine();
		}
	}

}
