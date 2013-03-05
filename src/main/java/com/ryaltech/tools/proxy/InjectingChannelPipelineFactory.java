package com.ryaltech.tools.proxy;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

public class InjectingChannelPipelineFactory implements ChannelPipelineFactory {

	public static final String HANDLER_ID="addressreplacinghandler";
	private ChannelPipelineFactory cpf;
	
	private ChannelHandler channelHandler;
	public InjectingChannelPipelineFactory(ChannelPipelineFactory cpf, ChannelHandler handler){
		this.cpf = cpf;
	
		channelHandler = handler;
		
	}
	public static ChannelPipelineFactory getPipelineFactory(ChannelPipelineFactory cpf, ChannelHandler handler){
		if(cpf instanceof InjectingChannelPipelineFactory)return cpf;
		return new InjectingChannelPipelineFactory(cpf, handler);
	}
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		/**
		 * TODO: to deal with chunks without much code should inject HttpChunkAggregator as per
		 * http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/http/HttpChunk.html
		 * This will cost us in memory, but currently we are not designing this for high volume.
		 */

		ChannelPipeline cp = cpf.getPipeline();
		if(cp.get(HANDLER_ID) != channelHandler)cp.addBefore("idleAware", HANDLER_ID, channelHandler);
		return cp;
	}

}
