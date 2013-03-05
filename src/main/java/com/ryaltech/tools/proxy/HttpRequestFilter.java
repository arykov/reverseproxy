package com.ryaltech.tools.proxy;



import org.jboss.netty.handler.codec.http.HttpRequest;

import com.ryaltech.tools.proxy.AddressMapper.Address;

/**
 * Implementors of this interface can be injected to change http request before
 * submitting it to the destination
 * 
 * @author arykov
 * 
 */
public interface HttpRequestFilter {
	/**
	 * 
	 * @param request HTTP request
	 * @param originalAddress original address request was directed to
	 * @param replacementAddress address request is ultimately sent to
	 * @return true if request changed false otherwise
	 */
	public boolean filterRequest(HttpRequest request, Address originalAddress,
			Address replacementAddress);

}
