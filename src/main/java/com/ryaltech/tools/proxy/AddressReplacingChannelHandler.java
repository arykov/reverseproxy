package com.ryaltech.tools.proxy;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.ryaltech.tools.proxy.AddressMapper.Address;

public class AddressReplacingChannelHandler extends
		SimpleChannelUpstreamHandler {

	public static final String HANDLER_ID = "AddressReplacer";
	private boolean defaultSecure;
	private AddressMapper mapper;
	private HttpRequestFilter filter;

	public AddressReplacingChannelHandler(AddressMapper mapper, HttpRequestFilter filter, boolean secure) {
		super();
		this.defaultSecure = secure;
		this.mapper = mapper;
		this.filter = filter;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent me)
			throws Exception {
		Object obj = me.getMessage();
		if (obj instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) obj;

			if (HttpMethod.CONNECT != request.getMethod()) {
				Address originalAddress = AddressMapper.fromHost(
						request.getHeader("Host"), defaultSecure);
				Address replacementAddress = mapper
						.getReplacementAddress(originalAddress);
				//filter intended to change the request
				filter.filterRequest(request, originalAddress, replacementAddress);
				if (replacementAddress != null) {

					String path = request.getUri();
					if (path.startsWith("http://")
							|| path.startsWith("https://")) {
						try {
							URL url = new URL(path);
							path = url.getFile();
							
						} catch (MalformedURLException e) {
							// ignore

						}
					}
					request.setUri(replacementAddress.toString() + path);
				}
			} else {
				Address originalAddress = AddressMapper.fromHost(
						request.getHeader("Host"), true);
				Address replacementAddress = mapper
						.getReplacementAddress(originalAddress);
				/**
				 * Following scenarios possible 1) straight through 2)
				 * https2http - the one we perform right now 3) https2https with
				 * name replacement 4) https2https but with man in the middle -
				 * no immideate plans to support
				 */
				// only act if request needs to be reverse proxied
				if (replacementAddress != null) {
					if (replacementAddress.isSecure()) {
						// https2https with name replacement
						request.setUri(replacementAddress.getHost() + ":"
								+ replacementAddress.getPort());

					} else {
						// https2http
						request.setUri(mapper.getHttpsProxyAddress()
								.getHostName()
								+ ":"
								+ mapper.getHttpsProxyAddress().getPort());
					}
				}
			}

		}
		super.messageReceived(ctx, me);
	}

}
