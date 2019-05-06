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
        ArrayList<String[]> matches = AddressParser.getInstance().getMatchesFromDefault("Haveforeningen Havebyen Mozart",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Haveforeningen Havebyen Mozart", "København SV", "2450"};
        assertArrayEquals(expectedMatch1, match1);
    }

    @Test
    public void testGetAddressesFromDefault1(){
        ArrayList<String[]> matches = AddressParser.getInstance().getMatchesFromDefault("Terrasserne",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Terrasserne", "Brønshøj", "2700"};
        String[] match2 = matches.get(1);
        String[] expectedMatch2 = new String[]{"Terrasserne", "Roskilde", "4000"};
        assertArrayEquals(expectedMatch1, match1);
        assertArrayEquals(expectedMatch2, match2);
    }

    //TODO Are two of these really necessary?
    @Test
    public void testGetAddressesFromDefault2(){
        ArrayList<String[]> matches = AddressParser.getInstance().getMatchesFromDefault("Rued Langgaards Vej",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Rued Langgaards Vej", "København S", "2300"};
        String[] match2 = matches.get(1);
        String[] expectedMatch2 = new String[]{"Rued Langgaards Vej", "Vejle", "7100"};
        assertArrayEquals(expectedMatch1, match1);
        assertArrayEquals(expectedMatch2, match2);
    }

    @Test
    public void testGetAddressesFromDefaultWithDanLetters(){
        ArrayList<String[]> matches = AddressParser.getInstance().getMatchesFromDefault("Højderyggen",false);
        String[] match1 = matches.get(0);
        String[] expectedMatch1 = new String[]{"Højderyggen","Vejle Øst","7120"};
        String[] match2 = matches.get(1);
        String[] expectedMatch2 = new String[]{"Højderyggen", "Herlev", "2730"};
        assertArrayEquals(expectedMatch1, match1);
        assertArrayEquals(expectedMatch2, match2);
    }
    //singleSearch testing

    @Test
    public void testAddressWithStreetHousenumberPostCodeAndCity(){
        Address address = AddressParser.getInstance().singleSearch("Tommelise 48,7500 Holstebro", );
        assertEquals("Tommelise", address.getStreetName().trim());
        assertEquals("48", address.getHouseNumber().trim());
        assertEquals("7500", address.getPostcode().trim());
        assertEquals("Holstebro", address.getCity().trim());
    }

    @Test
    public void testAddressWithLettersInHouseNumberWithDanLetters(){
        Address address = AddressParser.getInstance().singleSearch("Sauntevænget 5B,3100 Hornbæk", );
        assertEquals("Sauntevænget", address.getStreetName().trim());
        assertEquals("5B", address.getHouseNumber().trim());
        assertEquals("3100", address.getPostcode().trim());
        assertEquals("Hornbæk", address.getCity().trim());
    }

    @Test
    public void testAddressWithStreetFloorAndCityWithDanLetters(){
        Address address = AddressParser.getInstance().singleSearch("Månen 13, 1, 8850 Bjerringbro", );
        assertEquals("Månen", address.getStreetName().trim());
        assertEquals("13", address.getHouseNumber().trim());
        assertEquals("1", address.getFloor().trim());
        assertEquals("8850", address.getPostcode().trim());
        assertEquals("Bjerringbro", address.getCity().trim());
    }


    @Test
    public void testAddressWithStreetFloorSideAndCity(){
        Address address = AddressParser.getInstance().singleSearch("Valby Maskinfabriksvej 1, 1, TH. 2500 Valby ", );
            assertEquals("Valby Maskinfabriksvej", address.getStreetName().trim());
        assertEquals("1", address.getHouseNumber().trim());
        assertEquals("1", address.getFloor().trim());
        assertEquals("TH.", address.getSide().trim());
        assertEquals("2500", address.getPostcode().trim());
        assertEquals("Valby", address.getCity().trim());
    }

    @Test
    public void testAddressWithStreetAndPostcodeNoCity(){
        Address address = AddressParser.getInstance().singleSearch("Pingels Alle 47 3700", );
        assertEquals("Pingels Alle", address.getStreetName().trim());
        assertEquals("47", address.getHouseNumber().trim());
        assertEquals("3700", address.getPostcode().trim());
        assertEquals("Rønne", address.getCity().trim());
    }


    @Test
    public void testAddressWithStreetAndPostcodeMismatchingCity(){
        Address address = AddressParser.getInstance().singleSearch("Solskiftevej 8 2300 København", );
        assertEquals("Solskiftevej", address.getStreetName().trim());
        assertEquals("8", address.getHouseNumber().trim());
        assertEquals("2300", address.getPostcode().trim());
        assertEquals("København S", address.getCity().trim());
    }
}
