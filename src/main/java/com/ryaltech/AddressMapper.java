package com.ryaltech;

import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class AddressMapper {
	public static class Address {
		private String host;
		private int port;

		public Address(String host, int port, boolean secure) {
			super();
			this.host = host;
			this.port = port;
			this.secure = secure;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public boolean isSecure() {
			return secure;
		}

		private boolean secure;

		public boolean equals(Object obj) {
			if (obj instanceof Address && obj != null) {
				Address addr = (Address) obj;
				if ((addr.secure == secure) && (addr.port == port)) {
					if (addr.host == null) {
						return (host == null);
					} else {
						return addr.host.equals(host);
					}
				} else {
					return false;
				}

			} else {
				return false;
			}

		}

		public final int hashCode() {
			int hash = host.hashCode() + port;
			if (secure)
				hash *= 2;
			return hash;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(secure ? "https://" : "http://")
					.append(toHost());
			return sb.toString();
		}

		public String toHost() {
			if ((secure && port == 443) || (!secure && port == 80))
				return host;
			return host + ":" + port;
		}
	}

	private Map<Address, Address> conversions = new HashMap<Address, Address>();

	private InetSocketAddress httpsProxyAddress;

	AddressMapper(int httpsProxyPort) {
		httpsProxyAddress = new InetSocketAddress(httpsProxyPort);
	}

	public void addMapping(Address from, Address to)
			throws UnknownHostException {
		conversions.put(from, to);
	}

	public Address getReplacementAddress(Address originalAddress) {
		return conversions.get(originalAddress);
	}

	public InetSocketAddress getHttpsProxyAddress() {
		return httpsProxyAddress;
	}

	public static Address fromUrl(String strUrl)throws IllegalArgumentException{
		try {
			URL url = new URL(strUrl);
			int port = url.getPort();
			String host = url.getHost();
			if(null == host || "".equals(host)){
				throw new IllegalArgumentException(String.format("host in url %s is empty", strUrl ));
			}
			if(port == -1){
				port = url.getDefaultPort();
			}
			return new Address(url.getHost(), port, "https".equals(url.getProtocol()));			
		}catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
		
	}
	public static Address fromHost(String host, boolean secure)throws IllegalArgumentException {
		return fromUrl((secure?"https://":"http://")+host);		
	}

}
