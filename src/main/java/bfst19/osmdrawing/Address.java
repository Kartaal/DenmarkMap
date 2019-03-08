
package bfst19.osmdrawing;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Address {
	private final String street, house, floor, side, postcode, city;

	private static ArrayList<String> cities = new ArrayList<>();
	private static ArrayList<String> postcodes = new ArrayList<>();

	private Address(String _street, String _house, String _floor, String _side, String _postcode, String _city) {
		street = _street;
		house = _house;
		floor = _floor;
		side = _side;
		postcode = _postcode;
		city = _city;
	}

	public String toString() {
		/* if you want punctuation after floor,
		not in use because i might just switch to not accepting floor without punctuation.

		String floorWithPunctuation = floor;
		if(!floor.endsWith(".")){
			floorWithPunctuation = floor + ".";

		}*/
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

	final static String rHouse = "(?<house>([0-9]{1,3} ?[a-z]?))";
	final static String rFloor = "(?<floor>([1-9][0-9]?\\.?)|(st\\.)|ST\\.)";
	final static String rSide = "(?<side>th|tv|mf|TH|TV|MF|([0-9][0-9]?[0-9]?))";

	final static String[] regex = {

			//"(?<house>([0-9]{1,3} ?[a-z]?))",
			//rHouse+rFloor,
			//rHouse+rFloor+rSide,
			"^(?<house>([0-9]{1,3} ?[a-zA-Z]?))?,? ?(?<floor>([1-9]{1,3}\\.?)|(1st\\.)|(st\\.)|ST\\.)?,? ?(?<side>th\\.?|tv\\.?|mf\\.?|md\\.?|([0-9]{1,3}\\.?))?,?$"

			/*
			//simpel
			"^ *(?<street>[ÆØÅA-Zæøåa-z\\- ]+?)?[ \\.,]*(?<house>[0-9])? *$",
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
			*/
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

	public static Address parse(String adress){
		Builder b = new Builder();
		try {
			adress = adress.toLowerCase().trim();
			String streetMatch = checkStreet(adress, b);//this finds a potential street for the adress and if this is "", there is no match.
			if (!streetMatch.equals("")) {
				adress = adress.replace(streetMatch, ""); //if a match is found, it is removed from the string.
			}
			String[] cityMatch = betterCityCheck(adress); //same as the other one, but checking for cities/postcodes
			if (!(cityMatch[0].equals(""))) {
				adress = adress.replace(cityMatch[0], "");
				b.postcode = cityMatch[1];
				b.city = cityMatch[2];
			}
			//check the remaining part of the proposed adress against a pattern in regex
			adress = adress.trim();

			for (Pattern pattern : patterns) {
				Matcher match = pattern.matcher(adress);
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

	//This method checks if the start of the proposed adress matches any the streets found in the streenames file, and if a match is found, it is given to the builder and returned to be removed from the adress.
	public static String checkStreet(String adress,Builder b)throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/streetnames.txt"),"UTF-8"));
		String line = in.readLine();
		String mostCompleteMatch = "";
		while(line!=null){
			if(adress.startsWith(line.toLowerCase())){
				if(line.length()>mostCompleteMatch.length()){
					mostCompleteMatch=line;
				}
			}
			line = in.readLine();
		}
		b.street=mostCompleteMatch;
		return mostCompleteMatch.toLowerCase();
	}

	public static String[] betterCityCheck(String proposedAdress)throws Exception{
		if(cities.isEmpty()&&postcodes.isEmpty()){
			parseCitiesAndPostCodes();
		}
		String currentCity = "", currentPostcode = "",mostCompleteMatch = "",bestPostCodeMatch = "", bestCityMatch = "";

		for(int i=0;i<cities.size();i++){
			currentCity = cities.get(i);
			currentPostcode = postcodes.get(i);

			String checkSecondToLastToken = checkSecondToLastTokenForPostcode(proposedAdress, currentPostcode).toLowerCase();
			String checkThirdToLastToken = checkThirdToLastTokenForPostcode(proposedAdress, currentPostcode).toLowerCase();

			if(proposedAdress.endsWith(currentPostcode.toLowerCase() +" "+ currentCity.toLowerCase())){
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
			}else if((proposedAdress.endsWith(currentPostcode.toLowerCase()))){
				mostCompleteMatch = currentPostcode;
				bestCityMatch = currentCity;
				bestPostCodeMatch = currentPostcode;
			}else if(proposedAdress.endsWith(currentCity.toLowerCase())){
				mostCompleteMatch = currentCity;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}

		}
		//if not "" it means it has found a match inside the loop
		if(!(mostCompleteMatch.equals(""))){
			return new String[]{mostCompleteMatch,bestCityMatch,bestPostCodeMatch};
		}

		return new String[]{""};
	}

	//if second to last token in the proposed adress is the given postcode, it returns the part of the adress to remove, if not it returns "".
	public static String checkSecondToLastTokenForPostcode(String proposedAdress,String postcode){
		String[] adressTokens = proposedAdress.split(" ");
		if(adressTokens.length>=2&&adressTokens[adressTokens.length-2].equals(postcode)) {
			return (adressTokens[adressTokens.length - 2] + " " + adressTokens[adressTokens.length - 1]);
		}
		return "";
	}

	//if third to last token in the proposed adress is the given postcode, it returns the part of the adress to remove, if not it returns "".
	public static String checkThirdToLastTokenForPostcode(String proposedAdress,String postcode){
		String[] adressTokens = proposedAdress.split(" ");
		if(adressTokens.length>=3&&adressTokens[adressTokens.length-3].equals(postcode)) {
			return (adressTokens[adressTokens.length - 3] + " " + adressTokens[adressTokens.length - 2] + " " + adressTokens[adressTokens.length-1]);
		}
		return "";
	}

	public static void parseCitiesAndPostCodes()throws Exception{

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/postnumre.txt"),"UTF-8"));
		String line = in.readLine(); //the first line of the file starts with a ?, to combat this i've moved everything 1 line down, so now i skip the first line.
		line = in.readLine().toLowerCase();

		while(line!=null){
			String[] tokens = line.split(" ",2);
			postcodes.add(tokens[0]);
			cities.add(tokens[1]);
			line = in.readLine();
		}
	}

}
