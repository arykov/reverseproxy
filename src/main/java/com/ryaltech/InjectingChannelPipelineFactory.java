package com.ryaltech;

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

		ChannelPipeline cp = cpf.getPipeline();
		if(cp.get(HANDLER_ID) != channelHandler)cp.addBefore("idleAware", HANDLER_ID, channelHandler);
		return cp;
	}

}
