package bfst19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** documentation
 * This class is a singleton responsible for parsing search-strings
 * and returning available information from the given string.
 * To accomplish this, it uses a builder for keeping track of variables,
 * and uses 1 main method parseSearch, and 2 helper methods to do so.
 *
 * The first helper method is singlesearch, where it breaks down
 * the string into chunks and attempts to mactch it to data in the database
 * by first looking for a city, then looking in that city for streetnames
 * and then looking for a specific housenumber on that street.
 * The second is getMatchesFromDefault where it has utilizes binarysearch to search through
 * default collection of streetnames with matching city/postcode.
 *
 * parseSearch first calls singlesearch on the string, then looks at how much infomation
 * it found in the string and then returns different information based on the completeness the address
 * */
public class AddressParser {

	private static AddressParser addressParser = null;
	private static Model model = null;
	private String[] postcodes;
	private String[] cities;
	//a collection of the default searching file if there is no hit for cityCheck
	private static String[] defaults;

	public static AddressParser getInstance(){
		if(addressParser == null){
			return new AddressParser();
		}
		return addressParser;
	}

	public AddressParser(){
		addressParser = this;
	}

	/**
	 *
	 * @param proposedAddress
	 * @param model
	 *  * the propsedAddress is first given to singleSearch to attempt
	 *  * to find a single address to match the given string, if a complete
	 *  * address is not found, it returns different information depending on completeness
	 *  * if no streetname was found, it gets any matches from the default
	 *  * and returns, if no housenumber was found get the addresses on that street
	 *  * and return them, if the address has a streetname, a city/postcode and a housenumber
	 *  * then return it.
	 *  This method does not actually return anything, but does but the matches into model's observable
	 *  array of matches, and then notifies the observers.
	 */
	public void parseSearch(String proposedAddress, Model model) {
		model.clearMatches();
		Address a = AddressParser.getInstance().singleSearch(proposedAddress);
		//if the address does not have a city or a street name, get the string's matches from the default file and display them
		if(a.getStreetName().equals("Unknown") || a.getCity().equals("")) {
			ArrayList<String[]> possibleMatches =
					AddressParser.getInstance().getMatchesFromDefault(proposedAddress, false);

			if (possibleMatches != null) {
				model.clearMatches();
				for (String[] match : possibleMatches) {
					model.addFoundMatch(new String[]{match[0],match[1],match[2]});
				}
			}
		//if a address has a streetName it will also have a city because of the way singleSearch searches for street names
		}else if(a.getHouseNumber() == null){
			//if the house number is null, bet all the addresses house numbers from the streets file and display them
			ArrayList<String[]> possibleAddresses = AddressParser.getInstance().getAddress(a.getCity(),
					a.getPostcode(), a.getStreetName(),"",false);
			//if the house number is null, get all the addresses house numbers from the streets file and display them
			if (possibleAddresses != null) {
				model.clearMatches();
				String street = a.getStreetName();
				String city = a.getCity();
				String postcode = a.getPostcode();
				for (String[] match : possibleAddresses) {
					model.addFoundMatch(new String[]{street, match[2], city, postcode});
				}
			}
		}else{
			//if those 3 fields are filled, just put the address in, the ui will handle the rest
			model.clearMatches();
			model.addFoundMatch(new String[]{String.valueOf(a.getLon()),
					String.valueOf(a.getLat()), a.getStreetName(), a.getHouseNumber(),
					a.getFloor(), a.getSide(), a.getCity(), a.getPostcode()});
		}
		model.notifyFoundMatchesObservers();
	}

	/**
	 * @param proposedAddress
	 *  * Breaks the proposed address into 4 parts, checking the string for
	 *  * city/postcode removing recognized information
	 *  * doing the same for streetnames, then using regex to recognize housenumber, floor
	 *  * and side. Then if enough information is found it gets the remaining data
	 *  * from the database with getAddress and returns it to parseSearch.
	 * @return Address
	 */
	public Address singleSearch(String proposedAddress){
		Builder b = new Builder();
		proposedAddress = proposedAddress.toLowerCase().trim();
		String[] cityMatch = checkCity(proposedAddress);
		//it checks if a city is found in the cities.txt file or not and replaces it if found
		if(!(cityMatch[0].equals(""))) {
			proposedAddress = proposedAddress.replaceAll(cityMatch[0].toLowerCase(), "");
			b.city = cityMatch[1];
			b.postcode = cityMatch[2];
		}

		String streetMatch = checkStreet(proposedAddress, cityMatch);
		//if a city is found, we try to find a street in that cities streets.txt file that matches the proposed address
		if(!streetMatch.equals("")){
			proposedAddress = proposedAddress.replaceAll(streetMatch.toLowerCase(),"");
			b.streetName = streetMatch;
		}

		//this uses regex to find houseNumber, side and floor for the address after the cityCheck
		// and streetCheck have filtered the string.
		proposedAddress = proposedAddress.trim();
		for (Pattern pattern : patterns) {
			Matcher match = pattern.matcher(proposedAddress);
			if (match.matches()) {

					tryExtract(match, "house", b::houseNumber);
					tryExtract(match, "floor", b::floor);
					tryExtract(match, "side", b::side);

			}
		}

		//after all other things have been done, we find the latitude, longitude
		// and Id of the node that this address belongs to in the streetname file
		if(!b.streetName.equals("Unknown")&&!b.city.equals("")&&!b.postcode.equals("")){
			String city = b.city;
			String postcode = b.postcode;
			String streetname = b.streetName;
			String houseNumber = b.houseNumber;
			ArrayList<String[]> something = getAddress(city,postcode,streetname,houseNumber,true);

			String[] address = something.get(0);

			if(!address[0].equals("")) {
				b.lat = Float.valueOf(address[0]);
				b.lon = Float.valueOf(address[1]);
				b.houseNumber = address[2];
			}
		}
		return b.build();
	}

	/**
	 *
	 * @param city
	 * @param postcode
	 * @param streetName
	 * @param houseNumber
	 * @param singleSearch
	 *   * gets the data for specific address/street from a textfile with the given
	 *   * parameters and the helper class Texthandler.
	 *   * city, postcode and streetName is information needed to
	 *   * locate the address in the database, and the housenumber is used if a specific
	 *   * address on the street is required, indicated by the singleSearch boolean.
	 * @return Arraylist of address(es)
	 */
	//this method gets an address' remaining information from the streetname text file,
	// this information is called addressfields in this method, but is perhaps not the best name
	public ArrayList<String[]> getAddress( String city,
										   String postcode, String streetName,
										   String houseNumber, boolean singleSearch){
		ArrayList<String> addressesOnStreet = TextHandler.getInstance().getAddressesOnStreet(city,postcode,streetName);
		ArrayList<String[]> matches = new ArrayList<>();
		String[] addressFields;
		String address;
		if(singleSearch){
			if(houseNumber==null||houseNumber.equals("")){
				matches.add(new String[]{""});
				return matches;
			}
			for(int i = 0 ; i <= addressesOnStreet.size()-1 ; i++){
				address = addressesOnStreet.get(i);
				addressFields=address.split(" ");
				if(addressFields[2].toLowerCase().equalsIgnoreCase(houseNumber)){
					matches.add(addressFields);
					return matches;
				}
			}
		}else{
			for(int i = 0 ; i <= addressesOnStreet.size()-1 ; i++){
				address = addressesOnStreet.get(i);
				addressFields = address.split(" ");
				matches.add(addressFields);
			}
			return matches;
		}
		return null;
	}

	/**
	 * @param proposedAddress
	 * @param cityMatch
	 *   * Checks if the proposedAddress starts with any of the streets in the citymatch
	 *   * and if so returns the string to remove & the streetName
	 * @return partion to remove from proposedAddress and matching streetName
	 */
	//Checks if the start of the proposedAddress matches any the streets in the street names file
	//Checks if the start of the address matches any the streets in the street names file
	// if a match is found, the builders street field is set to the match
	// which is returned to be removed from the proposedAddress.
	public String checkStreet(String proposedAddress, String[] cityMatch) {
		if (cityMatch[0].equals("")) {
			return "";
		} else {
			ArrayList<String> streetsInCity = TextHandler.getInstance().getStreetsInCity(cityMatch[1], cityMatch[2]);
			String mostCompleteMatch = "";
			for (int i = 0; i < streetsInCity.size(); i++) {
				String line = streetsInCity.get(i);
				if (proposedAddress.startsWith(line.toLowerCase())) {
					if (line.length() > mostCompleteMatch.length()) {
						mostCompleteMatch = line;
					}
				}
			}
			return mostCompleteMatch;
		}
	}

	/**
	 *
	 * @param proposedAddress
	 * Checks the proposedAddress if it ends with either city, post or both
	 * from a list of cities+postcodes kept in Addressparser's memory as one of it's fields
	 * Here it assumes a postcode to be the most significant of a city and postcode,
	 * so if a pair is mismatched it assumes the postcode to be correct.
	 * Returns the string to be removed from the proposedAddress, and a city + postcode
	 * @return partition to remove from search-string and a matching city + +postcode
	 */
	public String[] checkCity(String proposedAddress){
		String currentCity = "", currentPostcode = "", mostCompleteMatch = "",
				bestPostCodeMatch = "", bestCityMatch = "";
		String[] match = new String[]{"","",""};

		for(int i = 0 ; i < cities.length ; i++){
			currentCity = cities[i];
			currentPostcode = postcodes[i];

			//this was motivated by using the postcode as the most significant part of a city and postcode,
			// so that if you write a postcode, it will use that postcodes matching city
			String postcodeCheck = checkThreeLastAddressTokensForPostcode(proposedAddress, currentPostcode).toLowerCase();
			//if the proposed address ends with the current postcode and city return those
			if(proposedAddress.endsWith(currentPostcode.toLowerCase() +" "+ currentCity.toLowerCase())) {
				mostCompleteMatch = currentPostcode + " " + currentCity;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}else if(proposedAddress.endsWith(currentCity.toLowerCase() +" "+ currentPostcode.toLowerCase())){
				mostCompleteMatch =  currentCity +  " " +currentPostcode;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
				//if a postcode is found in the last three tokens of the address return that postcode and matching city
			}else if(!(postcodeCheck.equals(""))){
				mostCompleteMatch = postcodeCheck;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
				//if a city is found at the end of the address, return that along with that cities postcode
				// (not 100% always accurate)
			}else if(proposedAddress.endsWith(currentCity.toLowerCase())){
				mostCompleteMatch = currentCity;
				bestPostCodeMatch = currentPostcode;
				bestCityMatch = currentCity;
			}
		}
		match[0] = mostCompleteMatch;
		match[1] = bestCityMatch;
		match[2] = bestPostCodeMatch;

		return match;
	}

	//if third to last token in the proposed address is the given postcode,
	// it returns the part of the address to remove, if not it returns "".
	public String checkThreeLastAddressTokensForPostcode(String proposedAddress, String postcode){
		String[] addressTokens = proposedAddress.split(" ");
		if(addressTokens.length>=1&&addressTokens[addressTokens.length-1].equals(postcode)){
			return addressTokens[addressTokens.length-1];
		}
		if(addressTokens.length>=2 && addressTokens[addressTokens.length-2].equals(postcode)) {
			return (addressTokens[addressTokens.length - 2] + " " + addressTokens[addressTokens.length - 1]);
		}
		if(addressTokens.length>=3 && addressTokens[addressTokens.length-3].equals(postcode)) {
			return (addressTokens[addressTokens.length - 3] + " " + addressTokens[addressTokens.length - 2]
					+ " " + addressTokens[addressTokens.length-1]);
		}
		return "";
	}

	/**
	 *
	 * @param proposedAddress
	 * @param singleSearch
	 * Uses a proposedAddress to look for a streetname in a collection of all streetnames
	 * in the database sorted lexicographically by the Address classes toString method
	 * It uses binarysearch to locate the given information for the given string and
	 * either returns the first found match, or all matches starting with the proposedAddress
	 * indicated by the singleSearch boolean
	 * @return match(s) from default
	 */
	//basically binary search using string.compareTo to determine if you should look in the upper or lower sub-array
	//it ignores case for compares, but returns the raw data
	public ArrayList<String[]> getMatchesFromDefault(String proposedAddress,boolean singleSearch){
		int lo = 0;
		int hi = defaults.length-1;
		int mid;
		while(lo<=hi){
			mid = lo+(hi-lo)/2;
			String currentDefault = defaults[mid].toLowerCase();
			if(currentDefault.startsWith(proposedAddress.toLowerCase())){
				if(singleSearch){
					ArrayList<String[]> result = new ArrayList<>();
					String[] matchTokens = currentDefault.split(" QQQ ");
					result.add(matchTokens);
					return result;
				}
				return traverseUpAndDown(mid,proposedAddress);
			}
			int resultOfComparison = proposedAddress.compareToIgnoreCase(currentDefault);
			if(resultOfComparison<0){
				hi = mid - 1;
			}else if (resultOfComparison>0){
				lo = mid + 1;
			}else{
				return traverseUpAndDown(mid,proposedAddress);
			}
		}
		return null;
	}

	//gets all possible matches from a given index,
	// from this index it traverses up and down the default array until it's no longer a match.
	//it also splits the match up in a string array where the first index is the street, second is city, third is postcode.
	private ArrayList<String[]> traverseUpAndDown(int mid,String proposedAddress) {
		ArrayList<String[]> matches = new ArrayList<>();
		int lo = mid-1;
		proposedAddress = proposedAddress.toLowerCase();
		String currentIndexStringRaw = defaults[mid];
		String currentIndexString = currentIndexStringRaw.toLowerCase();
		//traverses up the default array until it's no longer a match
		while(currentIndexString.startsWith(proposedAddress) && lo >= 0) {
			String[] matchTokens = currentIndexStringRaw.split(" QQQ ");
			matches.add(matchTokens);
			currentIndexStringRaw = defaults[lo];
			currentIndexString = currentIndexStringRaw.toLowerCase();
			lo--;
		}
		currentIndexStringRaw = defaults[mid+1];
		currentIndexString = currentIndexStringRaw.toLowerCase();
		int hi = mid + 2;
		//traverses down the default array until it's no longer a match
		while(currentIndexString.startsWith(proposedAddress) && hi < defaults.length){
			String[] matchTokens = currentIndexStringRaw.split(" QQQ ");
			matches.add(matchTokens);
            currentIndexStringRaw = defaults[hi];
            currentIndexString = currentIndexStringRaw.toLowerCase();
			hi++;
		}
		return matches;
	}

	/*<---------------------------Setup methods ------------------------------> */
	public void setDefaults(){
		ArrayList<String> defaults = TextHandler.getInstance().getDefault(Model.getDirPath());
		AddressParser.defaults = new String[defaults.size()];
		for(int i = 0 ; i < defaults.size() ; i++ ){
			AddressParser.defaults[i] = defaults.get(i);
		}
	}
	public void setCities(){
		ArrayList<String[]> citiesAndPostcodes = TextHandler.getInstance().parseCitiesAndPostcodes(Model.getDirPath());
		this.cities = new String[citiesAndPostcodes.size()];
		this.postcodes = new String[citiesAndPostcodes.size()];
		for(int i = 0 ; i < citiesAndPostcodes.size() ; i++){
			String[] tokens =citiesAndPostcodes.get(i);
			cities[i] = tokens[0];
			postcodes[i] = tokens[1];
		}
	}
	/*<----------------------------------------------------------------------> */

	/*<----------------------builder + regex methods ------------------------> */
	public class Builder {
		private int id;
		private float lat, lon;
		private String streetName = "Unknown", houseNumber="", postcode="", city="",floor="",side="";
		public Builder houseNumber(String _house)   { houseNumber = _house;   return this; }
		public Builder floor(String _floor)   { floor = _floor;   return this; }
		public Builder side(String _side)   { side = _side;   return this; }
		public Address build() {
			return new Address(id,lat,lon,streetName, houseNumber, postcode, city,floor,side);
		}
	}

	final String houseRegex = "(?<house>([0-9]{1,3} ?[a-zA-Z]?))?";
	final String floorRegex = "(?<floor>([1-9]{1,3}\\.?)|(1st\\.)|(st\\.))?";
	final String sideRegex = "(?<side>th\\.?|tv\\.?|mf\\.?|md\\.?|([0-9]{1,3}\\.?))?";

	//This only checks the remainder of the string at the end for house number, floor and side for the address.
	final String[] regex = {
			"^"+houseRegex+",? ?"+floorRegex+",? ?"+sideRegex+",?$"
	};

	/* Pattern:A regular expression, specified as a string, must first be compiled into an instance of this class
	 * Arrays.stream:Returns a sequential Stream with the specified array as its source
	 * Stream.map: Returns a stream consisting of the results of applying the given function to the elements of this stream.
	 * Pattern. compile: Compiles the given regular expression into a pattern.
	 */
	final Pattern[] patterns =
			Arrays.stream(regex).map(Pattern::compile).toArray(Pattern[]::new);

	/* Matcher:An engine that performs match operations on a character sequence by interpreting a Pattern.
	 * Consumer<String>: Represents an operation that accepts a single input argument and returns no result
	 * m. group: Returns the input subsequence captured by the given named-capturing group during the previous match operation.
	 * if the match was successful but the group specified failed to match any part of the input sequence, then null is returned.
	 */

	private void tryExtract(Matcher m, String group, Consumer<String> c) {
		try {
			c.accept(m.group(group));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(group);
		}
	}


}