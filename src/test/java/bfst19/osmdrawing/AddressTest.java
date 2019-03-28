package bfst19.osmdrawing;

import org.junit.Test;
import static org.junit.Assert.*;

public class AddressTest {
    @Test
    public void addressWithStreetAndHouse(){
        Address address = AddressParser.getInstance().parse("Rued Langaards Vej 7","denmark");
        assertEquals("Rued Langaards Vej", address.getStreetName().trim());
        assertEquals("7", address.getHouseNumber().trim());
    }

    @Test
    public void addressWithStreetAndCity(){
        Address address = AddressParser.getInstance().parse("Tommelise 48,7500 Holstebro","denmark");
        assertEquals("Tommelise", address.getStreetName().trim());
        assertEquals("48", address.getHouseNumber().trim());
        assertEquals("7500", address.getPostcode().trim());
        assertEquals("Holstebro", address.getCity().trim());
    }

    @Test
    public void addressWithStreetAndCityWithDanLetters(){
        Address address = AddressParser.getInstance().parse("Månen 13,8850 Bjerringbro","denmark");
        assertEquals("Månen", address.getStreetName().trim());
        assertEquals("13", address.getHouseNumber().trim());
        assertEquals("8850", address.getPostcode().trim());
        assertEquals("Bjerringbro", address.getCity().trim());
    }

    @Test
    public void addressWithStreetHouseSideCityDanLetters(){
        Address address = AddressParser.getInstance().parse("Valby Maskinfabriksvej 1, 1, 2500 Valby ","denmark");
        assertEquals("Valby Maskinfabriksvej", address.getStreetName().trim());
        assertEquals("1", address.getHouseNumber().trim());
        assertEquals("1", address.getFloor().trim());
        assertEquals("2500", address.getPostcode().trim());
        assertEquals("København", address.getCity().trim());
    }

    @Test
    public void addressWithStreetAndCityDanLetters(){
        Address address = AddressParser.getInstance().parse("Sauntevænget 5B,3100 Hornbæk","denmark");
        assertEquals("Sauntevænget", address.getStreetName().trim());
        assertEquals("5B", address.getHouseNumber().trim());
        assertEquals("3100", address.getPostcode().trim());
        assertEquals("Hornbæk", address.getCity().trim());
    }

}
