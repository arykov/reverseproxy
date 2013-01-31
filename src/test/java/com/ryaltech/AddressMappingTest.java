package com.ryaltech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ryaltech.AddressMapper.Address;

public class AddressMappingTest {

	@Test
	public void testFromHost() {
		// http
		assertEquals(new Address("host", 80, false),
				AddressMapper.fromHost("host", false));
		assertEquals(new Address("host", 123, false),
				AddressMapper.fromHost("host:123", false));

		// https
		assertEquals(new Address("host", 443, true),
				AddressMapper.fromHost("host", true));
		assertEquals(new Address("host1", 321, true),
				AddressMapper.fromHost("host1:321", true));

		try {
			AddressMapper.fromHost("host:1:1", false);
			fail();
		} catch (IllegalArgumentException ex) {
		}
		
		try {
			AddressMapper.fromHost("host:x", false);
			fail();
		} catch (IllegalArgumentException ex) {
		}
	}
	@Test
	public void testFromUrl() {
		// http
		assertEquals(new Address("host", 80, false),
				AddressMapper.fromUrl("http://host"));
		assertEquals(new Address("host", 123, false),
				AddressMapper.fromUrl("http://host:123"));

		// https
		assertEquals(new Address("host", 443, true),
				AddressMapper.fromUrl("https://host"));
		assertEquals(new Address("host1", 321, true),
				AddressMapper.fromUrl("https://host1:321"));
		
		try {
			AddressMapper.fromUrl("host");
			fail();
		} catch (IllegalArgumentException ex) {
		}
		
		try {
			Address address = AddressMapper.fromUrl("http://");
			fail();
		} catch (IllegalArgumentException ex) {
		}


		
	}

}
