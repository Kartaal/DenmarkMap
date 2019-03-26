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

    public Address parse(String proposedAddress, String country){
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
                    System.out.println(streetMatch[1]);
                    System.out.println(streetMatch[2]);
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

            if(!b.streetName.equals("Unknown")&&!b.city.equals("")){
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
    public String[] checkStreet(String address,String country,String[] cityMatch) throws IOException {
        BufferedReader in;
        String[] match = new String[]{"","",""};
        if (cityMatch[0].equals("")) {
            in = new BufferedReader(new InputStreamReader(new FileInputStream("data/" + country + "/streets.txt"), "UTF-8"));
            String line = in.readLine();
            String mostCompleteMatch = "";
            String mostCompleteMatchCity = "";
            String mostCompleteMatchPostcode = "";
            while(line.endsWith("$")){
                //splits at arbitrary dilimiter for proper splitting of streetnames and citynames
                //todo fix $ line end delimiter with for each city in country/cities.txt and for each street in country/city/streets.txt
                line = line.replace("$","");
                String[] tokens = line.split(" streetNameAndCityDelimiter ");
                String streetToken = tokens[0];
                //split is supposed to split at a delimiter and not include the delimiter in the tokens but it deosn't so i replace them instead
                String cityAndPostcodeToken = tokens[1].replace(" streetNameAndCityDelimiter ","");
                String[] cityAndPostcodeTokens = cityAndPostcodeToken.split(" cityAndPostcodeDelimiter ");
                String cityToken = cityAndPostcodeTokens[0];
                String postcodeToken = cityAndPostcodeTokens[1].replace(" cityAndPostcodeDelimiter ","");

                if(address.startsWith(streetToken.toLowerCase())){
                    if(streetToken.length() > mostCompleteMatch.length()){
                        mostCompleteMatch = tokens[0];
                        mostCompleteMatchCity = cityAndPostcodeTokens[0];
                        mostCompleteMatchPostcode = cityAndPostcodeTokens[1];
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
            in = new BufferedReader(new InputStreamReader(new FileInputStream("data/" + country + "/" + cityMatch[1] + " " + cityMatch[2]+ "/streets.txt"), "UTF-8"));
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

    public void parseCitiesAndPostCodes(String country)throws Exception{

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/"+country+"/cities.txt"),"UTF-8"));
        String line = in.readLine();

        while(line != null){
            String[] tokens = line.split(" cityAndPostcodeDelimiter ");
            cities.add(tokens[0]);
            String postcode = tokens[1].replace(" cityAndPostcodeDelimiter ","");
            postcodes.add(postcode);
            line = in.readLine();
        }
    }

}