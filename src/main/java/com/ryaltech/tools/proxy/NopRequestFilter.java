package com.ryaltech.tools.proxy;



import org.jboss.netty.handler.codec.http.HttpRequest;

import com.ryaltech.tools.proxy.AddressMapper.Address;

/**
 * Implementation that does nothing
 * 
 * @author arykov
 *
 */
public class NopRequestFilter implements HttpRequestFilter {

	@Override
	public boolean filterRequest(HttpRequest request, Address originalAddress,
			Address replacementAddress) {
		return false;
	}

}
