package bfst19.addressparser;

import org.junit.Test;
import static org.junit.Assert.*;

public class AddressParserTest {
    @Test 
	public void testAddressParseNonNull() {
		Address address = Address.parse("");
        assertNotNull("AddressParser.parse should not return null", address);
    }
}
