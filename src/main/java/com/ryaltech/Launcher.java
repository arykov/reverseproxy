package com.ryaltech;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpResponseFilters;
import org.littleshoot.proxy.SelfSignedKeyStoreManager;

public class Launcher {
	/**
	 * 
	 * @return any available listen port
	 * @throws RuntimeException
	 */
	public static int getAnyAvailablePort()throws RuntimeException{
		ServerSocket socket = null;		
		try {
			socket = new ServerSocket(0);			
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			try {
				if(socket != null)socket.close();
			} catch (IOException e) {
			}		
		}
		
		
	}
	/**
	 * Checks if listening on the port is possible.
	 * @param port
	 * @throws RuntimeException
	 */
	static void validatePortIsAvailable(int port)throws RuntimeException{
		ServerSocket socket = null;
		try{			
			socket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot listen on port %s. It is likely busy.", port), e);
		}finally{
			try {
				if(socket != null)socket.close();
			} catch (IOException e) {
			}		
		}
	}

	public static final int DEFAULT_HTTP_PROXY_PORT = 8080;
	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException {
		/*
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
        */
		

		int proxyPort=DEFAULT_HTTP_PROXY_PORT;
		int managementPort=-1;
		
		Properties props = new Properties();
		
		int i=0;

		try {
			while (i < args.length) {
				String flag = args[i];
				if (flag.equals("-proxyPort")) {
					proxyPort = Integer.parseInt(args[++i]);
				} else if (flag.equals("-managementPort")) {
					managementPort = Integer.parseInt(args[++i]);
				} else if (flag.equals("-propertyFile")) {
					props = new Properties();
					InputStream is = null;
					try {
						is = new FileInputStream(args[++i]);
						props.load(is);
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
							}
						}
					}
				}

				else {
					help();
					System.exit(-1);
				}
				i++;
			}
		} catch (Exception ex) {
			System.out.println("Failed parsing passed parameters.");
			help();
			ex.printStackTrace();
			System.exit(-1);
		}
    
		
		startServer(proxyPort, managementPort, props);
	}
	static HttpProxyServer httpsServer;
	static HttpProxyServer httpServer;
	static void startServer(int proxyPort, int managementPort, Properties props)
			throws UnknownHostException {
		int httpsProxyPort;
		try{
			httpsProxyPort = getAnyAvailablePort();
		}catch(Exception ex){
			System.out.println("Failed to find a port to listen for https proxy.");
			System.exit(-1);
			return;
		}
		
	    AddressMapper mapper = new AddressMapper(httpsProxyPort);
	    
	    for(Object key:props.keySet()){
	    	String from = (String)key;    		
    		String to = props.getProperty(from);
	    	try{
	    		mapper.addMapping(AddressMapper.fromUrl(from),AddressMapper.fromUrl(to)); 
	    	}catch(RuntimeException rex){
	    		System.out.println("The following entry in the property file '%s' dis invalid.");
	    		System.exit(-1);	    		
	    	}	    	
	    }
        
		httpsServer = new DefaultHttpProxyServer(httpsProxyPort,
				(HttpResponseFilters)null, null, 
	            new SelfSignedKeyStoreManager(), null, new NioClientSocketChannelFactory(), new HashedWheelTimer(), new ServerSocketChannelFactory(new Https2HttpChannelHandler(mapper)));
		httpsServer.start();
		final HttpProxyServer httpServer = new DefaultHttpProxyServer(proxyPort, 
	            (HttpResponseFilters)null, null, 
	            null, null,new NioClientSocketChannelFactory(), new HashedWheelTimer(), new ServerSocketChannelFactory(new AddressReplacingChannelHandler(mapper, false)));
		httpServer.start();
	}
	static void stopServer(){
		if(httpServer != null)
			httpServer.stop();
		if(httpsServer != null)
			httpsServer.stop();
		
	}

	private static void help() {
		System.out
				.println("java -classpath reverseproxy-<version>.jar [-proxyPort <proxyPort>] [-managementPort <managementPort>] [-propertyFile <propertyFile>]");
		System.out
				.println("\t<proxyPort> - port proxy listens on. It defaults to "
						+ DEFAULT_HTTP_PROXY_PORT);
		System.out
				.println("\t<managementPort> - port to start management server on.  If not specified management server will not start.");
		System.out
				.println("\t<propertyFile> - location to the file that contains settings.  If not specified none will be used.");
	}

}
