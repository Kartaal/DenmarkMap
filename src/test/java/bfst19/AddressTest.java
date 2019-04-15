package bfst19;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AddressTest {
    String datasetName = "denmark Database";
    Model model = new Model(datasetName);


    @Test
    public void testGetAddressFromDefault(){
        ArrayList<String[]> matches = AddressParser.getInstance(model).getMatchesFromDefault("Haveforeningen Havebyen Mozart",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Haveforeningen Havebyen Mozart", "København SV", "2450"};
        assertArrayEquals(expectedMatch1, match1);
    }

    @Test
    public void testGetAddressesFromDefaultWithDanLetters(){
        ArrayList<String[]> matches = AddressParser.getInstance(model).getMatchesFromDefault("Højderyggen",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Højderyggen","Vejle Øst","7120"};
        String[] match2 = matches.get(1);
        String[] expectedMatch2 = new String[]{"Højderyggen", "Herlev", "2730"};
        assertArrayEquals(expectedMatch1, match1);
        assertArrayEquals(expectedMatch2, match2);
    }

    @Test
    //this test does not work with our current implementation, as we abandon finding a match and simply ask the user which match they meant.
    public void testGetAddressesFromDefault1(){
        ArrayList<String[]> matches = AddressParser.getInstance(model).getMatchesFromDefault("Terrasserne",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Terrasserne", "Brønshøj", "2700"};
        String[] match2 = matches.get(1);
        String[] expectedMatch2 = new String[]{"Terrasserne", "Roskilde", "4000"};
        assertArrayEquals(expectedMatch1, match1);
        assertArrayEquals(expectedMatch2, match2);
    }

    //TODO Are two of these really necessary?
    @Test
    //this test does not work with our current implementation, as we abandon finding a match and simply ask the user which match they meant.
    public void testGetAddressesFromDefault2(){
        ArrayList<String[]> matches = AddressParser.getInstance(model).getMatchesFromDefault("Rued Langgaards Vej",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Rued Langgaards Vej", "København S", "2300"};
        String[] match2 = matches.get(1);
        String[] expectedMatch2 = new String[]{"Rued Langgaards Vej", "Vejle", "7100"};
        assertArrayEquals(expectedMatch1, match1);
        assertArrayEquals(expectedMatch2, match2);
    }


    @Test
    public void testAddressWithStreetAndCity(){
        Address address = AddressParser.getInstance(model).singleSearch("Tommelise 48,7500 Holstebro", datasetName);
        assertEquals("Tommelise", address.getStreetName().trim());
        assertEquals("48", address.getHouseNumber().trim());
        assertEquals("7500", address.getPostcode().trim());
        assertEquals("Holstebro", address.getCity().trim());
    }

    @Test
    public void testAddressWithStreetAndCityWithDanLetters(){
        Address address = AddressParser.getInstance(model).singleSearch("Månen 13,8850 Bjerringbro", datasetName);
        assertEquals("Månen", address.getStreetName().trim());
        assertEquals("13", address.getHouseNumber().trim());
        assertEquals("8850", address.getPostcode().trim());
        assertEquals("Bjerringbro", address.getCity().trim());
    }

    @Test
    //this doesn't work because it uses the postcode as the more significant than the given city name in the string
    public void testAddressWithStreetHouseSideCityDanLetters(){
        Address address = AddressParser.getInstance(model).singleSearch("Valby Maskinfabriksvej 1, 1, 2500 Valby ", datasetName);
        assertEquals("Valby Maskinfabriksvej", address.getStreetName().trim());
        assertEquals("1", address.getHouseNumber().trim());
        assertEquals("1", address.getFloor().trim());
        assertEquals("2500", address.getPostcode().trim());
        assertEquals("Valby", address.getCity().trim());
    }

    @Test
    public void testAddressWithStreetAndCityDanLetters(){
        Address address = AddressParser.getInstance(model).singleSearch("Sauntevænget 5B,3100 Hornbæk", datasetName);
        assertEquals("Sauntevænget", address.getStreetName().trim());
        assertEquals("5B", address.getHouseNumber().trim());
        assertEquals("3100", address.getPostcode().trim());
        assertEquals("Hornbæk", address.getCity().trim());
    }

}
