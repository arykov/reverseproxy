package com.ryaltech;

import java.net.UnknownHostException;

import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.HashedWheelTimer;
import org.littleshoot.proxy.ChainProxyManager;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpRequestFilter;
import org.littleshoot.proxy.HttpResponseFilters;
import org.littleshoot.proxy.SelfSignedKeyStoreManager;

import com.ryaltech.AddressMapper.Address;

public class Launcher {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException {
		final HttpResponseFilters responseFilters = new HttpResponseFilters() {
            public HttpFilter getFilter(final String hostAndPort) {
                return null;
            }
        };
        
        HttpRequestFilter requestFilter = new HttpRequestFilter() {
			
			@Override
			public void filter(HttpRequest httpRequest) {
				System.out.println(httpRequest);

				
			}
		};
        ChainProxyManager cpm = new ChainProxyManager(){

			@Override
			public String getChainProxy(HttpRequest httpRequest) {
				//httpRequest.setUri("http://cnn.com/");
				return null;
			}

			@Override
			public void onCommunicationError(String hostAndPort) {
				// TODO Auto-generated method stub
				
			}
        	
        };

        AddressMapper mapper = new AddressMapper(8081);
        
        mapper.addMapping(new Address("gazeta.ru", 80, false),new Address("lenta.ru", 80, false));
        mapper.addMapping(new Address("www.gazeta.ru", 80, false),new Address("lenta.ru", 80, false));
        mapper.addMapping(new Address("www.mail.com", 443, true), new Address("cnews.ru", 80, false));
		final HttpProxyServer server = new DefaultHttpProxyServer(8081,
				(HttpResponseFilters)null, cpm, 
	            new SelfSignedKeyStoreManager(), requestFilter, new NioClientSocketChannelFactory(), new HashedWheelTimer(), new ServerSocketChannelFactory(new Https2HttpChannelHandler(mapper)));
		server.start();
		final HttpProxyServer server1 = new DefaultHttpProxyServer(8080, 
	            (HttpResponseFilters)null, cpm, 
	            null, requestFilter,new NioClientSocketChannelFactory(), new HashedWheelTimer(), new ServerSocketChannelFactory(new AddressReplacingChannelHandler(mapper, false)));
		server1.start();
	}

}
