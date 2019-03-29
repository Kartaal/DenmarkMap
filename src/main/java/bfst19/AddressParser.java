package bfst19;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser {

    //todo add model as a field so it can ask directly for a text file.
    private static AddressParser addressParser = null;
    private ArrayList<String> postcodes = new ArrayList<>();
    private ArrayList<String> cities = new ArrayList<>();
    //a collection of the default searching file if there is no hit for cityCheck
    private ArrayList<String> defaults = new ArrayList<>();

    public static AddressParser getInstance(){
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



    //Todo: maybe add support for different order
    final String houseRegex = "(?<house>([0-9]{1,3} ?[a-z]?))";
    final String floorRegex = "(?<floor>([1-9][0-9]?\\.?)|(st\\.)|ST\\.)";
    final String sideRegex = "(?<side>th|tv|mf|TH|TV|MF|([0-9][0-9]?[0-9]?))";

    //This only checks the remainder of the string at the end for housenumber, floor and side for the adress.
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


    public ArrayList<String[]> parseNonSingleSearch(String proposeAddress,String country){
        return getMatchesFromDefault(proposeAddress,false);
    }

    //todo comments for this class
    public Address parseSingleSearch(String proposedAddress, String country){
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

            String streetMatch[] = checkStreet(proposedAddress,country,cityMatch);
            if(!streetMatch[0].equals("")){
                proposedAddress = proposedAddress.replaceAll(streetMatch[0],"");
                b.streetName = streetMatch[0];
                if(!streetMatch[1].equals("")){
                    b.city = streetMatch[1];
                    b.postcode = streetMatch[2];
                }
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

            if(!b.streetName.equals("Unknown")&&!b.city.equals("")&&!b.postcode.equals("")){
                String[] address = getAddress(country, b.city, b.postcode, b.streetName, b.houseNumber,singleSearch).get(0);
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

    private ArrayList<String[]> getAddress(String country, String city, String postcode, String streetName, String houseNumber,boolean singleSearch) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/"+country+"/"+city+ " QQQ " +postcode+"/"+streetName+".txt"),"UTF-8"));
        String address = in.readLine();
        ArrayList<String[]> matches = new ArrayList<>();
        String[] adressFields;

        if(singleSearch){
            if(houseNumber.equals("")){
                matches.add(address.split(" "));
                return matches;
            }
            //todo currently only returns the first line's address while it should find the line matching the housenumber
            while(address!=null){
                adressFields=address.split(" ");
                if(adressFields[3].toLowerCase().equals(houseNumber)){
                    matches.add(adressFields);
                    return matches;
                }
                address = in.readLine();
            }
        }else{
            while(address!=null){
                adressFields = address.split(" ");
                matches.add(adressFields);
            }
            return matches;
        }
        return null;
    }

    //Checks if start of the address matches any the streets in the street names file
    // if a match is found, the builders street field is set to the match
    // which is returned to be removed from the address.
    public String[] checkStreet(String address,String country,String[] cityMatch) throws IOException {
        BufferedReader in;
        String[] match = new String[]{"","",""};
        if (cityMatch[0].equals("")) {
            in = new BufferedReader(new InputStreamReader(new FileInputStream("data/" + country + "/streets.txt"), "UTF-8"));
            String line = in.readLine();
            String mostCompleteMatch = "";
            String mostCompleteMatchCity = "";
            String mostCompleteMatchPostcode = "";
            while(line!=null){
                //splits at arbitrary dilimiter for proper splitting of streetnames and citynames
                //todo fix $ line end delimiter with for each city in country/cities.txt and for each street in country/city/streets.txt
                line = line.replace("$","");
                String[] tokens = line.split(" QQQ ");
                String streetToken = tokens[0];
                String cityToken = tokens[1];
                String postcodeToken = tokens[2];
                //split is supposed to split at a delimiter and not include the delimiter in the tokens but it deosn't so i replace them instead

                if(address.startsWith(streetToken.toLowerCase())){
                    if(streetToken.length() > mostCompleteMatch.length()){
                        mostCompleteMatch = streetToken;
                        mostCompleteMatchCity = cityToken;
                        mostCompleteMatchPostcode = postcodeToken;
                    }
                }
                line = in.readLine();
            }
            System.out.println(mostCompleteMatch);
            System.out.println(mostCompleteMatchCity);
            System.out.println(mostCompleteMatchPostcode);
            match[0] = mostCompleteMatch;
            match[1] = mostCompleteMatchCity;
            match[2] = mostCompleteMatchPostcode;
        } else {
            System.out.println("city: "+cityMatch[1]+"postcode: "+cityMatch[2]);
            in = new BufferedReader(new InputStreamReader(new FileInputStream("data/" + country + "/" + cityMatch[1] + " QQQ " + cityMatch[2]+ "/streets.txt"), "UTF-8"));
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
            match[0] = mostCompleteMatch;
        }
        return match;
    }

    public String[] CityCheck(String proposedAddress) throws Exception{
        String currentCity = "", currentPostcode = "", mostCompleteMatch = "",
                bestPostCodeMatch = "", bestCityMatch = "";
        String[] match = new String[]{"","",""};

        for(int i = 0 ; i < cities.size() ; i++){
            currentCity = cities.get(i);
            currentPostcode = postcodes.get(i);

            //this was motivated by using the postcode as the most significant part of a city and postcode,
            // so that if you write a postcode, it will use that postcodes matching city
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
        match[0] = mostCompleteMatch;
        match[1] = bestCityMatch;
        match[2] = bestPostCodeMatch;

        return match;
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

    public ArrayList<String[]> getAdresses(String filepath){
        ArrayList<String[]> addresses = new ArrayList<>();
        return addresses;
    }


    public void parseDefaults(String filepath){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath),"UTF-8"));
            String line = in.readLine();
            while(line!=null){
                defaults.add(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseCitiesAndPostCodes(String country)throws Exception{

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/"+country+"/cities.txt"),"UTF-8"));
        String line = in.readLine();

        while(line != null){
            String[] tokens = line.split(" QQQ ");
            cities.add(tokens[0]);
            String postcode = tokens[1].replace(" QQQ ","");
            postcodes.add(postcode);
            line = in.readLine();
        }
    }

}