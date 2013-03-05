package com.ryaltech.tools.proxy;

import static org.junit.Assert.*;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.ryaltech.tools.proxy.AddressMapper.Address;

public class RequestRecorder {
	private URL lastRequestUrl;

	public void requestRecieved(HttpServletRequest request) {
		try {
			lastRequestUrl = new URL(request.getScheme(),
					request.getServerName(), request.getServerPort(),
					request.getQueryString() == null ? request.getRequestURI()
							: request.getRequestURI() + '?'
									+ request.getQueryString());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void assertUrl(String url) throws Exception {
		URL actual = getLastRequestAndReset();
		if (url != null) {
			assertNotNull(actual);

			assertEquals(normalize(new URL(url)).toString(), normalize(actual)
					.toString());
		} else {
			assertNull(actual);
		}
	}

	private URL normalize(URL url) throws Exception {

		Address address = AddressMapper.fromUrl(url.toString());
		return new URL(address.toString() + url.getFile());

	}

	private URL getLastRequestAndReset() {
		URL actual = lastRequestUrl;
		lastRequestUrl = null;
		return actual;
	}

}
