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
import org.junit.Test;
import org.littleshoot.proxy.KeyStoreManager;
import org.littleshoot.proxy.SelfSignedKeyStoreManager;
//@Ignore
public class IntegrationTest {
	private static final String HTTPS_SERVER1_SIGNATURE = "httpsServer1";
	private static final String HTTP_SERVER1_SIGNATURE = "httpServer1";
	private static Server httpServer1;
	private static Server httpsServer1;
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
		ctxFactory.setKeyManagerPassword(new String(new SelfSignedKeyStoreManager().getKeyStorePassword()));
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
	public static void startServers()throws Exception {
		httpServer1 = startHttpJetty(new DummyHttpServlet(HTTP_SERVER1_SIGNATURE));
		System.out.println(getServerPort(httpServer1));
		httpsServer1 = startHttpsJetty(new DummyHttpServlet(HTTPS_SERVER1_SIGNATURE));
		System.out.println(getServerPort(httpsServer1));

	}

	@AfterClass
	public static void stopServers() throws Exception {
		httpServer1.stop();
		httpsServer1.stop();
	}

	String getHttpResponse(HttpClient httpClient, String host, int port, String path)
			throws IOException, NoSuchAlgorithmException {
		
		HttpGet request = new HttpGet(
				String.format("%s:%s%s", host, port, path));
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

	private HttpClient createHttpClient(HttpHost proxy) throws NoSuchAlgorithmException {
		HttpClient client = new DefaultHttpClient();
		if(proxy != null){
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
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

	/**
	 * Makes sure that everything started fine
	 * @throws Exception
	 */
	@Test
	public void testStartedServersDirectly() throws Exception{
		//no proxy
		HttpClient client = createHttpClient(null);
		assertEquals(HTTP_SERVER1_SIGNATURE, getHttpResponse(client, "http://localhost", getServerPort(httpServer1), ""));
		assertEquals(HTTPS_SERVER1_SIGNATURE, getHttpResponse(client, "https://localhost", getServerPort(httpsServer1), ""));
		
	}
	
	@Test
	public void testProxyStraightThrough() throws Exception {
		int launcherPort = Launcher.getAnyAvailablePort();
		Launcher.startServer(launcherPort, -1, new Properties());
		try {
			HttpClient client = createHttpClient(new HttpHost("localhost",
					launcherPort, "http"));
			assertEquals(
					HTTP_SERVER1_SIGNATURE,
					getHttpResponse(client, "http://localhost",
							getServerPort(httpServer1), ""));
			assertEquals(
					HTTPS_SERVER1_SIGNATURE,
					getHttpResponse(client, "https://localhost",
							getServerPort(httpsServer1), ""));
		} finally {
			Launcher.stopServer();
		}
	}

}
