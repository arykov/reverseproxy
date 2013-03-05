package com.ryaltech.tools.proxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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

		//TODO: identify when either http or https proxy or management server don't startup
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
					props = readProperties(args[++i]);
					
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

	private static Properties readProperties(String fileName) throws IOException {
		Properties props = new Properties();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			
			String line = br.readLine();
			if(line != null){
				String [] chunks = line.split("=");
				if(chunks.length == 2){
					props.put(chunks[0], chunks[1]);
				}
			}
			
		} finally {
			br.close();
		}
		return props;
		
	}

	static HttpProxyServer httpsServer;
	static HttpProxyServer httpServer;
	static Server managementServer;
	static AddressMapper startServer(int proxyPort, int managementPort, Properties props)
			throws UnknownHostException {
		int httpsProxyPort;
		try{
			httpsProxyPort = getAnyAvailablePort();
		}catch(Exception ex){
			System.out.println("Failed to find a port to listen for https proxy.");
			System.exit(-1);
			return null;
		}
		
	    AddressMapper mapper = new AddressMapper(httpsProxyPort);
	    
	    
	    try{
	    	mapper.loadMappings(props); 
	    }catch(RuntimeException rex){
	    	System.out.println("Invalid mapping properties."+props.toString());
	    	System.exit(-1);	    		
	    }	    	
        
		httpsServer = new DefaultHttpProxyServer(httpsProxyPort,
				(HttpResponseFilters)null, null, 
	            new SelfSignedKeyStoreManager(), null, new NioClientSocketChannelFactory(), new HashedWheelTimer(), new ServerSocketChannelFactory(new AddressReplacingChannelHandler(mapper, true)));
		httpsServer.start();
		final HttpProxyServer httpServer = new DefaultHttpProxyServer(proxyPort, 
	            (HttpResponseFilters)null, null, 
	            null, null,new NioClientSocketChannelFactory(), new HashedWheelTimer(), new ServerSocketChannelFactory(new AddressReplacingChannelHandler(mapper, false)));
		httpServer.start();
		if(managementPort > 0){
			managementServer = startManagementServer(managementPort, mapper);
		}
		return mapper;
	}
	
	static Server startManagementServer(int port, AddressMapper mapper){
		Servlet servlet = new AddressMapperManagementServlet(mapper);
	
		Server server = new Server(port);
		
		ServletContextHandler servletHandler = new ServletContextHandler();	
		servletHandler.setContextPath("/addressmap");
		
		servletHandler.addServlet(new ServletHolder(servlet), "/*");		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });

		
		try {			
			resourceHandler.setResourceBase(Launcher.class.getResource("/web").toURI().toString());
			HandlerList list = new HandlerList();			
			list.addHandler(servletHandler);
			list.addHandler(resourceHandler);
			server.setHandler(list);
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to start management server", ex);
		}

		return server;

	}

	static void stopServer(){
		if(httpServer != null)
			httpServer.stop();
		if(httpsServer != null)
			httpsServer.stop();
		if(managementServer != null)
			try{
				managementServer.stop();
			}catch(Exception ex){}
		
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
