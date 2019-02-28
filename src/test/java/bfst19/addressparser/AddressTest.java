package bfst19.addressparser;

import org.junit.Test;
import static org.junit.Assert.*;

public class AddressTest {
    @Test 
	public void testAddressParseNonNull() {
		Address address = Address.parse("");
        assertNotNull("Address.parse should not return null", address);
    }
}
