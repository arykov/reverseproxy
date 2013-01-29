package com.ryaltech;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.ServerSocketChannel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class ServerSocketChannelFactory extends NioServerSocketChannelFactory {

	private ChannelHandler handler;
	public ServerSocketChannelFactory(ChannelHandler handler){ 
		super();
		this.handler = handler;
		
	}
	@Override
	public void releaseExternalResources() {
		super.releaseExternalResources();

	}

	@Override
	public ServerSocketChannel newChannel(ChannelPipeline pipeline) {
		pipeline.addLast("serverHandler", new ServerChannelHandler(handler));
		ServerSocketChannel channel = super.newChannel(pipeline);
		
		return channel;
	}

}
