
package bfst19.osmdrawing;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser {

	private static AddressParser addressParser = null;
	private ArrayList<String> postcodes = new ArrayList<>();
	private ArrayList<String> cities = new ArrayList<>();

	public static AddressParser AddressParser(){
		if(addressParser == null){
			addressParser = new AddressParser();
		}
		return addressParser;
	}


	public class Builder {
		private long id;
		private float lat, lon;
		private String streetName = "Unknown", houseNumber="", postcode="", city="",floor="",side="";
		public Builder houseNumber(String _house)   { houseNumber = _house;   return this; }
		public Builder floor(String _floor)   { floor = _floor;   return this; }
		public Builder side(String _side)   { side = _side;   return this; }
		public Address build() {
			return new Address(id,lat,lon,streetName, houseNumber, postcode, city,floor,side);
		}
	}




	final String houseRegex = "(?<house>([0-9]{1,3} ?[a-z]?))";
	final String floorRegex = "(?<floor>([1-9][0-9]?\\.?)|(st\\.)|ST\\.)";
	final String sideRegex = "(?<side>th|tv|mf|TH|TV|MF|([0-9][0-9]?[0-9]?))";

	final String[] regex = {
			"^(?<house>([0-9]{1,3} ?[a-zA-Z]?))?,? ?(?<floor>([1-9]{1,3}\\.?)|(1st\\.)|(st\\.))?,? ?(?<side>th\\.?|tv\\.?|mf\\.?|md\\.?|([0-9]{1,3}\\.?))?,?$"
	};

	final Pattern[] patterns =
			Arrays.stream(regex).map(Pattern::compile).toArray(Pattern[]::new);

	private void tryExtract(Matcher m, String group, Consumer<String> c) {
		try {
			c.accept(m.group(group));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public Address Parse(String proposedAddress, String country){
		proposedAddress = proposedAddress.toLowerCase().trim();
		try {
			parseCitiesAndPostCodes(country);
			Builder b = new Builder();
			String[] cityMatch = CityCheck(proposedAddress);

			if (!(cityMatch[0].equals(""))) {
				proposedAddress = proposedAddress.replaceAll(cityMatch[0].toLowerCase(), "");
				b.city = cityMatch[1];
				b.postcode = cityMatch[2];
			}

			String streetMatch = checkStreet(proposedAddress,country,cityMatch);
			if(!streetMatch.equals("")){
				proposedAddress = proposedAddress.replaceAll(streetMatch,"");
				b.streetName = streetMatch;
			}

			proposedAddress = proposedAddress.trim();
			for (Pattern pattern : patterns) {
				Matcher match = pattern.matcher(proposedAddress);
				if (match.matches()) {
					tryExtract(match, "house", b::houseNumber);
					tryExtract(match, "floor", b::floor);
					tryExtract(match, "side", b::side);
				}
			}

			if(!b.streetName.equals("Unknown")&&!b.city.equals("")){
				System.out.println("housenumber "+b.houseNumber);
				String[] address = getAddress(country, b.city, b.postcode, b.streetName, b.houseNumber);
				if(address!=null) {
					b.id = Long.valueOf(address[0]);
					b.lat = Float.valueOf(address[1]);
					b.lon = Float.valueOf(address[2]);
					b.houseNumber = address[3];
				}
			}

			return b.build();

		} catch (Exception e) {
			//TODO:handle this differently, so it doesn't just return null if failure
			e.printStackTrace();
		}
		return null;
	}

	private String[] getAddress(String country, String city, String postcode, String streetName, String houseNumber) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/"+country+"/"+city+ " " +postcode+"/"+streetName+".txt"),"UTF-8"));
		String address = in.readLine();
		String[] adressFields;


		if(houseNumber.equals("")){
			return address.split(" ");
		}
		while(address!=null){
			adressFields=address.split(" ");
			if(adressFields[3].toLowerCase().equals(houseNumber)){
				return adressFields;
			}
			address = in.readLine();
		}
		return null;
	}

	//Checks if start of the address matches any the streets in the street names file
	// if a match is found, the builders street field is set to the match
	// which is returned to be removed from the address.
	public String checkStreet(String address,String country,String[] cityMatch) throws IOException {
		BufferedReader in;
		if(cityMatch[0].equals("")){

			in = new BufferedReader(new InputStreamReader(new FileInputStream("data/"+country+"/streets.txt"),"UTF-8"));
		}else{
			in = new BufferedReader(new InputStreamReader(new FileInputStream("data/" + country + "/" + cityMatch[1] + " " + cityMatch[2] + "/streets.txt"),"UTF-8"));
		}
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
		System.out.println(mostCompleteMatch);
		return mostCompleteMatch.toLowerCase();
	}

	public String[] CityCheck(String proposedAddress) throws Exception{
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
	public String checkSecondToLastTokenForPostcode(String proposedAdress,String postcode){
		String[] addressTokens = proposedAdress.split(" ");
		if(addressTokens.length>=2 && addressTokens[addressTokens.length-2].equals(postcode)) {
			return (addressTokens[addressTokens.length - 2] + " " + addressTokens[addressTokens.length - 1]);
		}
		return "";
	}

	//if third to last token in the proposed address is the given postcode,
	// it returns the part of the address to remove, if not it returns "".
	public String checkThirdToLastTokenForPostcode(String proposedAdress,String postcode){
		String[] adressTokens = proposedAdress.split(" ");
		if(adressTokens.length>=3 && adressTokens[adressTokens.length-3].equals(postcode)) {
			return (adressTokens[adressTokens.length - 3] + " " + adressTokens[adressTokens.length - 2]
							+ " " + adressTokens[adressTokens.length-1]);
		}
		return "";
	}

	public void parseCitiesAndPostCodes(String country)throws Exception{

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/"+country+"/cities.txt"),"UTF-8"));
		String line = in.readLine().toLowerCase();

		while(line != null){
			String[] tokens = line.split(" ",2);
			cities.add(tokens[0]);
			postcodes.add(tokens[1]);
			line = in.readLine();
		}
	}

}
