package filters;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.ryaltech.tools.proxy.AddressMapper.Address;
import com.ryaltech.tools.proxy.HttpRequestFilter;

public class WebLogicSsl implements HttpRequestFilter {
	public static final String WL_PROXY_SSL_HEADER = "WL-Proxy-SSL";
	public static final String WL_PROXY_SSL_HEADER_VALUE = "true";
	

	@Override
	public boolean filterRequest(HttpRequest request, Address originalAddress,
			Address replacementAddress) {
		if (originalAddress.isSecure() && !replacementAddress.isSecure()) {
			request.addHeader(WL_PROXY_SSL_HEADER, WL_PROXY_SSL_HEADER_VALUE);
			return true;
		}else{
			return false;
		}
	}

	

}
