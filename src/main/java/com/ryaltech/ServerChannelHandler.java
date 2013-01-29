package com.ryaltech;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DefaultChildChannelStateEvent;

public class ServerChannelHandler implements ChannelUpstreamHandler {
	private ChannelHandler handler;

	public ServerChannelHandler(ChannelHandler handler) {
		super();
		this.handler = handler;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		if (e instanceof DefaultChildChannelStateEvent
				&& ((DefaultChildChannelStateEvent) e).getChildChannel()
						.isOpen()) {

			e.getChannel()
					.getConfig()
					.setPipelineFactory(
							InjectingChannelPipelineFactory
									.getPipelineFactory(e.getChannel()
											.getConfig().getPipelineFactory(),
											handler));
		}

		ctx.sendUpstream(e);

	}

}
