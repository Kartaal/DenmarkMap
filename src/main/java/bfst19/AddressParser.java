package bfst19;

import bfst19.Exceptions.RegexGroupNonexistentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            throw new RegexGroupNonexistentException(group);
        }
    }

    public Address singleSearch(String proposedAddress){
        Builder b = new Builder();
        proposedAddress = proposedAddress.toLowerCase().trim();
        String[] cityMatch = CityCheck(proposedAddress);
        //it checks if a city is found in the cities.txt file or not and replaces it if found
        if(!(cityMatch[0].equals(""))) {
            proposedAddress = proposedAddress.replaceAll(cityMatch[0].toLowerCase(), "");
            b.city = cityMatch[1];
            b.postcode = cityMatch[2];
        }

        String streetMatch = checkStreet(proposedAddress,cityMatch);
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
                try {
                    tryExtract(match, "house", b::houseNumber);
                    tryExtract(match, "floor", b::floor);
                    tryExtract(match, "side", b::side);
                } catch (RegexGroupNonexistentException e) {
                    //TODO Test for this exception being thrown
                    System.out.println(e.getMessage());
                }
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
            for(String string : address){
                System.out.println(string);
            }
            if(!address[0].equals("")) {
                b.lat = Float.valueOf(address[0]);
                b.lon = Float.valueOf(address[1]);
                b.houseNumber = address[2];
            }
        }
        return b.build();
    }

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

    //Checks if the start of the address matches any the streets in the street names file
    // if a match is found, the builders street field is set to the match
    // which is returned to be removed from the address.
    public String checkStreet(String address,String[] cityMatch) {
        if (cityMatch[0].equals("")) {
            return "";
        } else {
            ArrayList<String> streetsInCity = TextHandler.getInstance().getStreetsInCity(cityMatch[1], cityMatch[2]);
            String mostCompleteMatch = "";
            for (int i = 0; i < streetsInCity.size(); i++) {
                String line = streetsInCity.get(i);
                if (address.startsWith(line.toLowerCase())) {
                    if (line.length() > mostCompleteMatch.length()) {
                        mostCompleteMatch = line;
                    }
                }
            }
            return mostCompleteMatch;
        }
    }

    public String[] CityCheck(String proposedAddress){
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
        String currentIndexString = defaults[mid];
        //traverses up the default array until it's no longer a match
        while(currentIndexString.toLowerCase().startsWith(proposedAddress)){
            String[] matchTokens = currentIndexString.split(" QQQ ");
            matches.add(matchTokens);
            currentIndexString = defaults[lo];
            lo--;
        }
        currentIndexString = defaults[mid+1];
        int hi = mid + 2;
        //traverses down the default array until it's no longer a match
        while(currentIndexString.toLowerCase().startsWith(proposedAddress)){
            String[] matchTokens = currentIndexString.split(" QQQ ");
            matches.add(matchTokens);
            currentIndexString = defaults[hi];
            hi++;
        }
        return matches;
    }

    public void setDefaults(){
        ArrayList<String> defaults = TextHandler.getInstance().getDefault(Model.getDirPath());
        this.defaults = new String[defaults.size()];
        for(int i = 0 ; i < defaults.size() ; i++ ){
            this.defaults[i] = defaults.get(i);
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


}