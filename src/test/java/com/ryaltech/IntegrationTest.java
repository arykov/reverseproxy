package com.ryaltech;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.Servlet;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.littleshoot.proxy.KeyStoreManager;
import org.littleshoot.proxy.SelfSignedKeyStoreManager;

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

	private static final String PROXIED_URL1 = "http://1";
	private static final String PROXIED_URL2 = "http://1:80";
	private static final String PROXIED_URL3 = "http://1:1";
	private static final String PROXIED_URL4 = "https://1";
	private static final String PROXIED_URL5 = "https://1:443";
	private static final String PROXIED_URL6 = "https://1:1";
	private static final String PROXIED_URL7 = "https://1:2";
	private static final String PROXIED_URL8 = "https://1:3";
	private static final String PROXIED_URL9 = "https://2";
	private static final String PROXIED_URL10 = "https://2:443";

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

		// start proxy
		proxyPort = Launcher.getAnyAvailablePort();
		Launcher.startServer(proxyPort, -1, new Properties() {
			{
				put(PROXIED_URL1, "http://localhost:"+getServerPort(httpServer1));
				put(PROXIED_URL3, "http://localhost:"+getServerPort(httpServer2));
				put(PROXIED_URL4, "https://localhost:"+getServerPort(httpsServer1));
				put(PROXIED_URL6, "https://localhost:"+getServerPort(httpsServer2));
				put(PROXIED_URL7, "http://localhost:"+getServerPort(httpServer3));
				put(PROXIED_URL8, "http://localhost:"+getServerPort(httpServer4));
				put(PROXIED_URL9, "http://localhost:"+getServerPort(httpServer5));
			}
		});

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

	String getHttpResponse(HttpClient httpClient, String url) throws IOException, NoSuchAlgorithmException {

		HttpGet request = new HttpGet(url);
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
		assertServersNoTranforms(createHttpClient(null));
	}

	@Test
	public void testProxyStraightThrough() throws Exception {
		// through proxy
		assertServersNoTranforms(createHttpClient(new HttpHost("localhost",
				proxyPort, "http")));
	}
	
	@Test
	public void testProxyHttp2Http()throws Exception{
		HttpClient client = createHttpClient(new HttpHost("localhost",
				proxyPort, "http"));
		assertEquals(
				HTTP_SERVER1_SIGNATURE,
				getHttpResponse(client, PROXIED_URL1));
		
		assertEquals(
				HTTP_SERVER1_SIGNATURE,
				getHttpResponse(client, PROXIED_URL2));
		
		assertEquals(
				HTTP_SERVER2_SIGNATURE,
				getHttpResponse(client, PROXIED_URL3));
	}

	//TODO: Fix
	@Ignore
	@Test
	public void testProxyHttps2Https()throws Exception{
		HttpClient client = createHttpClient(new HttpHost("localhost",
				proxyPort, "http"));
		assertEquals(
				HTTPS_SERVER1_SIGNATURE,
				getHttpResponse(client, PROXIED_URL4));
		
		assertEquals(
				HTTPS_SERVER1_SIGNATURE,
				getHttpResponse(client, PROXIED_URL5));
		
		assertEquals(
				HTTPS_SERVER2_SIGNATURE,
				getHttpResponse(client, PROXIED_URL6));
	}

	//TODO: Fix
	@Ignore
	@Test
	public void testProxyHttps2Http()throws Exception{
		HttpClient client = createHttpClient(new HttpHost("localhost",
				proxyPort, "http"));
		assertEquals(
				HTTP_SERVER3_SIGNATURE,
				getHttpResponse(client, PROXIED_URL7));
		
		assertEquals(
				HTTP_SERVER4_SIGNATURE,
				getHttpResponse(client, PROXIED_URL8));
		
		assertEquals(
				HTTP_SERVER5_SIGNATURE,
				getHttpResponse(client, PROXIED_URL9));
		assertEquals(
				HTTP_SERVER5_SIGNATURE,
				getHttpResponse(client, PROXIED_URL10));
	}

}
