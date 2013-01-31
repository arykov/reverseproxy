package com.ryaltech;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.security.KeyStore;

import javax.servlet.Servlet;

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

@Ignore
public class IntegrationTest {
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
		httpServer1 = startHttpJetty(new DummyHttpServlet("httpServer1"));
		System.out.println(getServerPort(httpServer1));
		httpsServer1 = startHttpsJetty(new DummyHttpServlet("httpsServer1"));
		System.out.println(getServerPort(httpsServer1));

	}

	@AfterClass
	public static void stopServers() throws Exception {
		httpServer1.stop();
		httpsServer1.stop();
	}

	@Test
	public void test() throws Exception{
		Thread.sleep(1000000);
	}

}
