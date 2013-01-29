package com.ryaltech;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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


	public static Address fromHost(String host, boolean secure) {
		if (host == null)
			throw new IllegalArgumentException("host cannot be null");
		String chunks[] = host.split(":");
		int port = secure ? 443 : 80;
		if (chunks.length == 2) {
			try {
				port = Integer.parseInt(chunks[1]);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Illegal port format", nfe);
			}
			host = chunks[0];
		}
		if (chunks.length > 2) {
			throw new IllegalArgumentException("Too many semicolons. Max 1");
		}
		return new Address(host, port, secure);

	}

}
