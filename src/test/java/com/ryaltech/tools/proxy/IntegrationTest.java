package com.ryaltech.tools.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.Servlet;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littleshoot.proxy.KeyStoreManager;
import org.littleshoot.proxy.SelfSignedKeyStoreManager;

import com.google.gson.Gson;

//@Ignore
public class IntegrationTest {
	
	private static final String HTTPS_SERVER1_SIGNATURE = "httpsServer1";
	private static final String HTTPS_SERVER2_SIGNATURE = "httpsServer2";

	private static final String HTTP_SERVER1_SIGNATURE = "httpServer1";
	private static final String HTTP_SERVER2_SIGNATURE = "httpServer2";
	private static final String HTTP_SERVER3_SIGNATURE = "httpServer3";
	private static final String HTTP_SERVER4_SIGNATURE = "httpServer4";
	private static final String HTTP_SERVER5_SIGNATURE = "httpServer5";

	private static Server httpServer1;
	private static Server httpServer2;
	private static Server httpServer3;
	private static Server httpServer4;
	private static Server httpServer5;
	private static Server httpsServer1;
	private static Server httpsServer2;
	private static int proxyPort;
	private static int managementPort;
	
	private static AddressMapper mapper;
	private static Properties defaultMappings;

	
	private static final String PROXIED_URL1 = "http://test1";
	private static final String PROXIED_URL2 = "http://test1:80";
	private static final String PROXIED_URL3 = "http://test1:8111";
	private static final String PROXIED_URL4 = "https://test1";
	private static final String PROXIED_URL5 = "https://test1:443";
	private static final String PROXIED_URL6 = "https://test1:8111";
	private static final String PROXIED_URL7 = "https://test1:8222";
	private static final String PROXIED_URL8 = "https://test1:8333";
	private static final String PROXIED_URL9 = "https://test2";
	private static final String PROXIED_URL10 = "https://test2:443";
	
	
	private HttpClient directHttpClient;
	private HttpClient proxiedHttpClient;

	// private Server httpsServer1

	static int getServerPort(Server server) {
		return server.getConnectors()[0].getPort();
	}

	static KeyStore readKeyStore() throws Exception {
		KeyStoreManager ksm = new SelfSignedKeyStoreManager();
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

		// get user password and file input stream
		char[] password = ksm.getKeyStorePassword();

		InputStream is = ksm.keyStoreAsInputStream();
		try {
			ks.load(is, password);
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return ks;
	}

	static Server startHttpsJetty(Servlet servlet) throws Exception {
		Server server = new Server();
		SslContextFactory ctxFactory = new SslContextFactory();
		ctxFactory.setKeyStore(readKeyStore());
		ctxFactory.setKeyManagerPassword(new String(
				new SelfSignedKeyStoreManager().getKeyStorePassword()));
		SslSocketConnector connector = new SslSocketConnector(ctxFactory);
		connector.setPort(Launcher.getAnyAvailablePort());
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(servlet), "/*");

		server.start();

		return server;
	}

	static Server startHttpJetty(Servlet servlet) throws Exception {
		Server server = new Server(Launcher.getAnyAvailablePort());

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(servlet), "/*");
		server.start();

		return server;

	}

	@BeforeClass
	public static void startServers() throws Exception {
		httpServer1 = startHttpJetty(new DummyHttpServlet(
				HTTP_SERVER1_SIGNATURE));
		

		httpServer2 = startHttpJetty(new DummyHttpServlet(
				HTTP_SERVER2_SIGNATURE));
		httpServer3 = startHttpJetty(new DummyHttpServlet(
				HTTP_SERVER3_SIGNATURE));
		httpServer4 = startHttpJetty(new DummyHttpServlet(
				HTTP_SERVER4_SIGNATURE));
		httpServer5 = startHttpJetty(new DummyHttpServlet(
				HTTP_SERVER5_SIGNATURE));

		httpsServer1 = startHttpsJetty(new DummyHttpServlet(
				HTTPS_SERVER1_SIGNATURE));

		httpsServer2 = startHttpsJetty(new DummyHttpServlet(
				HTTPS_SERVER2_SIGNATURE));

		System.out.println(HTTP_SERVER1_SIGNATURE+" port:"+getServerPort(httpServer1));
		System.out.println(HTTP_SERVER2_SIGNATURE+" port:"+getServerPort(httpServer2));
		System.out.println(HTTP_SERVER3_SIGNATURE+" port:"+getServerPort(httpServer3));
		System.out.println(HTTP_SERVER4_SIGNATURE+" port:"+getServerPort(httpServer4));
		System.out.println(HTTP_SERVER5_SIGNATURE+" port:"+getServerPort(httpServer5));
		System.out.println(HTTPS_SERVER1_SIGNATURE+" port:"+getServerPort(httpsServer1));
		System.out.println(HTTPS_SERVER2_SIGNATURE+" port:"+getServerPort(httpsServer2));
		defaultMappings = new Properties() {
			{
				put(PROXIED_URL1, "http://localhost:"+getServerPort(httpServer1));
				put(PROXIED_URL3, "http://localhost:"+getServerPort(httpServer2));
				put(PROXIED_URL4, "https://localhost:"+getServerPort(httpsServer1));
				put(PROXIED_URL6, "https://localhost:"+getServerPort(httpsServer2));
				put(PROXIED_URL7, "http://localhost:"+getServerPort(httpServer3));
				put(PROXIED_URL8, "http://localhost:"+getServerPort(httpServer4));
				put(PROXIED_URL9, "http://localhost:"+getServerPort(httpServer5));
			}};
			

		// start proxy
		try{
			proxyPort = Integer.getInteger("proxyPort").intValue();
		}catch(Exception ex){
			proxyPort = Launcher.getAnyAvailablePort();			
		}
		System.out.println("Proxy port: "+proxyPort);
		
		//might cause infinite loop
		do{
			managementPort = Launcher.getAnyAvailablePort();
		}while(proxyPort == managementPort);



		mapper = Launcher.startServer(proxyPort, managementPort, new NopRequestFilter(), defaultMappings);
		


	}

	@AfterClass
	public static void stopServers() throws Exception {
		httpServer1.stop();
		httpServer2.stop();
		httpServer3.stop();
		httpServer4.stop();
		httpServer5.stop();

		httpsServer1.stop();
		httpsServer2.stop();
		Launcher.stopServer();
	}

	@Before
	public void setUp()throws Exception{
		directHttpClient = createHttpClient(null);
		proxiedHttpClient = createHttpClient(new HttpHost("localhost",
				proxyPort, "http"));
		
	}
	@After
	public void tearDown(){
		directHttpClient.getConnectionManager().shutdown();
		proxiedHttpClient.getConnectionManager().shutdown();
	}
	String getHttpResponse(HttpClient httpClient, String url) throws NoSuchAlgorithmException, IOException{
		return getHttpResponse(httpClient, new HttpGet(url));
	}
	String getHttpResponse(HttpClient httpClient, HttpRequestBase request) throws IOException, NoSuchAlgorithmException {

		
		HttpResponse response = httpClient.execute(request);
		StringBuffer textView = new StringBuffer();
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		String line = "";
		while ((line = rd.readLine()) != null) {
			textView.append(line);
		}
		return textView.toString();
	}
	
	String getHttpResponse(HttpClient httpClient, String host, int port,
			String path) throws IOException, NoSuchAlgorithmException {
		return getHttpResponse(httpClient, String.format("%s:%s%s", host, port, path));
		
	}

	private HttpClient createHttpClient(HttpHost proxy)
			throws NoSuchAlgorithmException {
		HttpClient client = new DefaultHttpClient();
		if (proxy != null) {
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}

		SSLContext sslContext;

		sslContext = SSLContext.getInstance("SSL");

		// set up a TrustManager that trusts everything
		try {
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {

					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {

				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {

				}
			} }, new SecureRandom());
		} catch (KeyManagementException e) {
		}
		SSLSocketFactory ssf = new SSLSocketFactory(sslContext,
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = client.getConnectionManager();

		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", 443, ssf));
		return client;
	}

	public void assertServersNoTranforms(HttpClient client) throws Exception {
		// no proxy

		assertEquals(
				HTTP_SERVER1_SIGNATURE,
				getHttpResponse(client, "http://localhost",
						getServerPort(httpServer1), ""));
		assertEquals(
				HTTP_SERVER2_SIGNATURE,
				getHttpResponse(client, "http://localhost",
						getServerPort(httpServer2), ""));
		assertEquals(
				HTTP_SERVER3_SIGNATURE,
				getHttpResponse(client, "http://localhost",
						getServerPort(httpServer3), ""));
		assertEquals(
				HTTP_SERVER4_SIGNATURE,
				getHttpResponse(client, "http://localhost",
						getServerPort(httpServer4), ""));
		assertEquals(
				HTTP_SERVER5_SIGNATURE,
				getHttpResponse(client, "http://localhost",
						getServerPort(httpServer5), ""));

		assertEquals(
				HTTPS_SERVER1_SIGNATURE,
				getHttpResponse(client, "https://localhost",
						getServerPort(httpsServer1), ""));
		assertEquals(
				HTTPS_SERVER2_SIGNATURE,
				getHttpResponse(client, "https://localhost",
						getServerPort(httpsServer2), ""));

	}

	/**
	 * Makes sure that everything started fine
	 * 
	 * @throws Exception
	 */
	@Test	
	public void testStartedServersDirectly() throws Exception {
		// no proxy
		assertServersNoTranforms(directHttpClient);
	}

	@Test	
	public void testProxyStraightThrough() throws Exception {
		// through proxy
		assertServersNoTranforms(proxiedHttpClient);
	}


	@Test	
	public void testProxyHttp2Http()throws Exception{
				
		assertEquals(
				HTTP_SERVER1_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL1));
		
		assertEquals(
				HTTP_SERVER1_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL2));
		
		assertEquals(
				HTTP_SERVER2_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL3));
	}

	
	@Test
	public void testProxyHttps2Https()throws Exception{
		
		assertServersNoTranforms(proxiedHttpClient);
		
		
		assertEquals(
				HTTPS_SERVER1_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL4));
		
		assertEquals(
				HTTPS_SERVER1_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL5));
		
		assertEquals(
				HTTPS_SERVER2_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL6));
	}

	
	@Test	
	public void testProxyHttps2Http()throws Exception{
		
		
		assertEquals(
				HTTP_SERVER3_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL7));
				
		
		assertEquals(
				HTTP_SERVER4_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL8));
		
		assertEquals(
				HTTP_SERVER5_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL9));
		assertEquals(
				HTTP_SERVER5_SIGNATURE,
				getHttpResponse(proxiedHttpClient, PROXIED_URL10));
	}
	
	@Test
	public void testMapper()throws Exception{
		Properties changedMappings = new Properties() {
			{
				put(PROXIED_URL1, "http://localhost:"+getServerPort(httpServer2));
				put(PROXIED_URL3, "http://localhost:"+getServerPort(httpServer3));
				put(PROXIED_URL4, "https://localhost:"+getServerPort(httpsServer2));
				put(PROXIED_URL6, "https://localhost:"+getServerPort(httpsServer1));
				put(PROXIED_URL7, "http://localhost:"+getServerPort(httpServer4));
				put(PROXIED_URL8, "http://localhost:"+getServerPort(httpServer5));
				put(PROXIED_URL9, "http://localhost:"+getServerPort(httpServer1));
			}};
			mapper.loadMappings(changedMappings);
			try{
				//http2http
				assertEquals(
						HTTP_SERVER2_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL1));
				
				assertEquals(
						HTTP_SERVER2_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL2));
				
				assertEquals(
						HTTP_SERVER3_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL3));
				assertServersNoTranforms(proxiedHttpClient);
				
				//https2https
				assertEquals(
						HTTPS_SERVER2_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL4));
				
				assertEquals(
						HTTPS_SERVER2_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL5));
				
				assertEquals(
						HTTPS_SERVER1_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL6));
				
				
				//https2http
				assertEquals(
						HTTP_SERVER4_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL7));
						
				
				assertEquals(
						HTTP_SERVER5_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL8));
				
				assertEquals(
						HTTP_SERVER1_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL9));
				assertEquals(
						HTTP_SERVER1_SIGNATURE,
						getHttpResponse(proxiedHttpClient, PROXIED_URL10));
				
			}finally{
				mapper.loadMappings(defaultMappings);
			}
	}
	
	@Test
	public void testManagementOps()throws Exception{
		Gson gson = new Gson();
		
		//GET
		String response = getHttpResponse(directHttpClient, "http://localhost:"+managementPort+"/addressmap");
		assertNotNull(response);
		Map<String, String> map = gson.fromJson(response, HashMap.class);
		assertEquals(mapper.getMappings(), map);
		
		//PUT
		final String original = "http://origin.com";
		final String replacement = "http://replacement.com";
		
		
		assertNull(mapper.getMappings().get(original));
		assertNull(mapper.getReplacementAddress(AddressMapper.fromUrl(original)));
		response = getHttpResponse(directHttpClient, new HttpPut("http://localhost:"+managementPort+"/addressmap/"+original+"/"+replacement));
		assertNotNull(response);
		map = gson.fromJson(response, HashMap.class);
		assertEquals(mapper.getMappings(), map);
		assertNotNull(mapper.getMappings().get(original));
		assertNotNull(mapper.getReplacementAddress(AddressMapper.fromUrl(original)));
		assertEquals(replacement, mapper.getReplacementAddress(AddressMapper.fromUrl(original)).toString());

		//DELETE
		response = getHttpResponse(directHttpClient, new HttpDelete("http://localhost:"+managementPort+"/addressmap/"+original));
		assertNotNull(response);
		map = gson.fromJson(response, HashMap.class);
		assertEquals(mapper.getMappings(), map);
		assertNull(mapper.getMappings().get(original));
		assertNull(mapper.getReplacementAddress(AddressMapper.fromUrl(original)));
		
		
		//TODO: not happy scenarios
	}
	

}
