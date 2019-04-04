package bfst19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser {

    private static AddressParser addressParser = null;
    private static Model model = null;
    private ArrayList<String> postcodes = new ArrayList<>();
    private ArrayList<String> cities = new ArrayList<>();
    //a collection of the default searching file if there is no hit for cityCheck
    private ArrayList<String> defaults = new ArrayList<>();

    public static AddressParser getInstance(Model model){
        if(addressParser == null){
            return new AddressParser(model);
        }
        return addressParser;
    }

    public AddressParser(Model model){
        addressParser = this;
        this.model = model;
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



    //Todo: maybe add support for different order
    final String houseRegex = "(?<house>([0-9]{1,3} ?[a-zA-Z]?))?";
    final String floorRegex = "(?<floor>([1-9]{1,3}\\.?)|(1st\\.)|(st\\.))?";
    final String sideRegex = "(?<side>th\\.?|tv\\.?|mf\\.?|md\\.?|([0-9]{1,3}\\.?))?";

    //This only checks the remainder of the string at the end for housenumber, floor and side for the adress.
    final String[] regex = {
            "^"+houseRegex+",? ?"+floorRegex+",? ?"+sideRegex+",?$"
    };

    /* Pattern:A regular expression, specified as a string, must first be compiled into an instance of this class
     * Arrays.stresm:Returns a sequential Stream with the specified array as its source
     * Stream.map: Returns a stream consisting of the results of applying the given function to the elements of this stream.
     * Patteren. compile: Compiles the given regular expression into a pattern.
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
            e.printStackTrace();
        }
    }
    
    //todo comments for this class
    public Address singleSearch(String proposedAddress, String country){
        proposedAddress = proposedAddress.toLowerCase().trim();
        Builder b = new Builder();
        String[] cityMatch = CityCheck(proposedAddress);

        //it checks if a city is found in the cities.txt file or not and replaces it if found
        if (!(cityMatch[0].equals(""))) {
            proposedAddress = proposedAddress.replaceAll(cityMatch[0].toLowerCase(), "");
            b.city = cityMatch[1];
            b.postcode = cityMatch[2];
        }

        String streetMatch = checkStreet(proposedAddress,country,cityMatch);
        //if a city is found, we try to find a street in that cities streets.txt file that matches the proposed address
        if(!streetMatch.equals("")){
            proposedAddress = proposedAddress.replaceAll(streetMatch,"");
            b.streetName = streetMatch;
        }

        //this uses regex to find houseNumber, side and floor for the address after the cityCheck and streetCheck have filtered the string.
        proposedAddress = proposedAddress.trim();
        for (Pattern pattern : patterns) {
            Matcher match = pattern.matcher(proposedAddress);
            if (match.matches()) {
                tryExtract(match, "house", b::houseNumber);
                tryExtract(match, "floor", b::floor);
                tryExtract(match, "side", b::side);
            }
        }

        //after all other things have been done, we find the lattitude, longettiude
        // and Id of the node that this address belongs to in the streetname's file
        if(!b.streetName.equals("Unknown")&&!b.city.equals("")&&!b.postcode.equals("")){
            String[] address = getAddress(country, b.city, b.postcode, b.streetName, b.houseNumber,true).get(0);
            if(address!=null) {
                b.id = Long.valueOf(address[0]);
                b.lat = Float.valueOf(address[1]);
                b.lon = Float.valueOf(address[2]);
                b.houseNumber = address[3];
            }
        }
        return b.build();
    }

    //this method gets an address' remaining information from the streetname's text file,
    // this information is called addressfields in this method, but is perhaps not the best name
    public ArrayList<String[]> getAddress(String country, String city, String postcode, String streetName, String houseNumber,boolean singleSearch){
        ArrayList<String> adressesOnStreet = model.getAddressesOnStreet(country,city,postcode,streetName);
        String address = adressesOnStreet.get(0);
        ArrayList<String[]> matches = new ArrayList<>();
        String[] adressFields;

        if(singleSearch){
            if(houseNumber.equals("")){
                matches.add(address.split(" "));
                return matches;
            }
            for(int i = 1 ; i < adressesOnStreet.size()-1 ; i++){
                address = adressesOnStreet.get(i);
                adressFields=address.split(" ");
                if(adressFields[3].toLowerCase().equalsIgnoreCase(houseNumber)){
                    matches.add(adressFields);
                    return matches;
                }
            }
        }else{
            for(int i = 1 ; i < adressesOnStreet.size()-1 ; i++){
                address = adressesOnStreet.get(i);
                adressFields = address.split(" ");
                matches.add(adressFields);
            }
            return matches;
        }
        return null;
    }

    //Checks if the start of the address matches any the streets in the street names file
    // if a match is found, the builders street field is set to the match
    // which is returned to be removed from the address.
    public String checkStreet(String address,String country,String[] cityMatch) {
        ArrayList<String> cities = model.getStreetsInCity(country,cityMatch[1],cityMatch[2]);
        String mostCompleteMatch = "";
        for(int i = 0; i<cities.size();i++){
            String line = cities.get(i);
            if(address.startsWith(line.toLowerCase())){
                if(line.length() > mostCompleteMatch.length()){
                    mostCompleteMatch = line.toLowerCase();
                }
            }
        }
        return mostCompleteMatch;
    }

    public String[] CityCheck(String proposedAddress){
        String currentCity = "", currentPostcode = "", mostCompleteMatch = "",
                bestPostCodeMatch = "", bestCityMatch = "";
        String[] match = new String[]{"","",""};

        for(int i = 0 ; i < cities.size() ; i++){
            currentCity = cities.get(i);
            currentPostcode = postcodes.get(i);

            //this was motivated by using the postcode as the most significant part of a city and postcode,
            // so that if you write a postcode, it will use that postcodes matching city
            String postcodeCheck = checkThreeLastAdressTokensForPostcode(proposedAddress, currentPostcode).toLowerCase();

            //if the proposed address ends with the current postcode and city return those
            if(proposedAddress.endsWith(currentPostcode.toLowerCase() +" "+ currentCity.toLowerCase())){
                mostCompleteMatch = currentPostcode +" "+ currentCity;
                bestPostCodeMatch = currentPostcode;
                bestCityMatch = currentCity;

                //if a postcode is found in the last three tokens of the address return that postcode and matching city
            }else if(!(postcodeCheck.equals(""))){
                mostCompleteMatch = postcodeCheck;
                bestPostCodeMatch = currentPostcode;
                bestCityMatch = currentCity;

                //if a city is found at the end of the address, return that along with that cities postcode (not 100% always accurate)
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
    public String checkThreeLastAdressTokensForPostcode(String proposedAdress,String postcode){
        String[] addressTokens = proposedAdress.split(" ");
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
        int hi = defaults.size()-1;
        int mid = 0;
        while(lo<=hi){
            mid = lo+(hi-lo)/2;
            String currentDefault = defaults.get(mid).toLowerCase();
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
        String currentIndexString = defaults.get(mid);
        //traverses up the default array until it's no longer a match
        while(currentIndexString.toLowerCase().startsWith(proposedAddress)){
            String[] matchTokens = currentIndexString.split(" QQQ ");
            matches.add(matchTokens);
            currentIndexString = defaults.get(lo);
            lo--;
        }
        currentIndexString = defaults.get(mid+1);
        int hi = mid + 2;
        //traverses down the default array until it's no longer a match
        while(currentIndexString.toLowerCase().startsWith(proposedAddress)){
            String[] matchTokens = currentIndexString.split(" QQQ ");
            matches.add(matchTokens);
            currentIndexString = defaults.get(hi);
            hi++;
        }
        return matches;
    }

    public void setDefaults(ArrayList<String> defaults){
        this.defaults = defaults;
    }

    public void parseCitiesAndPostCodes(ArrayList<String> citiesTextFile){
        for(int i = 0;i<citiesTextFile.size()-1;i++){
            String line = citiesTextFile.get(i);
            String[] tokens = line.split(" QQQ ");
            cities.add(tokens[0]);
            String postcode = tokens[1].replace(" QQQ ","");
            postcodes.add(postcode);
        }
    }

}