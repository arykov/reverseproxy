package com.ryaltech;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.ryaltech.AddressMapper.Address;

public class AddressReplacingChannelHandler extends
		SimpleChannelUpstreamHandler {

	public static final String HANDLER_ID = "AddressReplacer";
	private boolean defaultSecure;
	private AddressMapper mapper;

	public AddressReplacingChannelHandler(AddressMapper mapper, boolean secure) {
		super();
		this.defaultSecure = secure;
		this.mapper = mapper;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent me)
			throws Exception {
		Object obj = me.getMessage();
		if (obj instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) obj;
			if (HttpMethod.CONNECT != request.getMethod()) {
				// should we read weblogic header to determing secure?
				// problem arises when we come back to http proxy after https
				Address originalAddress = AddressMapper.fromHost(
						request.getHeader("Host"), defaultSecure);
				Address replacementAddress = mapper
						.getReplacementAddress(originalAddress);
				if (replacementAddress != null) {

					String path = request.getUri();
					if (path.startsWith("http")) {
						try {
							URL url = new URL(path);
							path = url.getPath();
						} catch (MalformedURLException e) {
							// ignore

						}
					}
					request.setUri(replacementAddress.toString() + path);
				} 
			}else {
				/**
				 * TODO: right now every connect request get https proxied. In reality there are 3 scenarios:
				 * 1) No need to do anything. Regular https request outside
				 * 2) https2http - the one we perform right now
				 * 3) https2https with name replacement
				 * 4) https2https but with man in the middle - no immideate plans to support
				 */
				
				 
				request.setUri(mapper.getHttpsProxyAddress().getHostName()
						+ ":" + mapper.getHttpsProxyAddress().getPort());
			}

		}
		super.messageReceived(ctx, me);
	}

}
