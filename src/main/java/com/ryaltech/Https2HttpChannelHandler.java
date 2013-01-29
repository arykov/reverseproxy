package com.ryaltech;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.ryaltech.AddressMapper.Address;

public class Https2HttpChannelHandler extends SimpleChannelUpstreamHandler {
	private AddressMapper mapper;

	public Https2HttpChannelHandler(AddressMapper mapper) {
		super();
		this.mapper = mapper;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent me)
			throws Exception {

		final Object obj = me.getMessage();
		// don't deal with chunks yet
		if (obj instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) obj;
			String host = request.getHeader("Host");
			//we know this is only used for https
			Address originalAddress = AddressMapper.fromHost(host, true);
			Address replacementAddress = mapper.getReplacementAddress(originalAddress);
			String uri = request.getUri();
			if (!uri.startsWith("http://") && !uri.startsWith("https://"))
				request.setUri("http://" + replacementAddress.getHost() + ":"
						+ replacementAddress.getPort() + uri);

		}

		super.messageReceived(ctx, me);
	}

}
