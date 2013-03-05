package com.ryaltech.tools.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import filters.WebLogicSsl;

public class HttpFilterTest {
	@Test
	public void testNopFilter(){
		HttpRequestFilter filter = new NopRequestFilter();
		assertFalse(filter.filterRequest(null, null, null));
	}
	
	@Test
	public void testWebLogicSslFilter(){
		HttpRequestFilter filter = new WebLogicSsl();
		
		HeaderTestingRequest request = new HeaderTestingRequest();
		assertFalse(filter.filterRequest(request, AddressMapper.fromUrl("https://xyz"), AddressMapper.fromUrl("https://yyy")));
		assertFalse(request.isHeaderSet());
		
		request = new HeaderTestingRequest();
		assertFalse(filter.filterRequest(request, AddressMapper.fromUrl("http://xyz"), AddressMapper.fromUrl("http://yyy")));
		assertFalse(request.isHeaderSet());
		
		request = new HeaderTestingRequest();
		assertTrue(filter.filterRequest(request, AddressMapper.fromUrl("https://xyz"), AddressMapper.fromUrl("http://yyy")));
		assertTrue(request.isHeaderSet());
		
	}
	static class HeaderTestingRequest implements HttpRequest {

		@Override
		public String getHeader(String name) {
			
			return null;
		}

		@Override
		public List<String> getHeaders(String name) {
			
			return null;
		}

		@Override
		public List<Entry<String, String>> getHeaders() {
			
			return null;
		}

		@Override
		public boolean containsHeader(String name) {
			
			return false;
		}

		@Override
		public Set<String> getHeaderNames() {
			
			return null;
		}

		@Override
		public HttpVersion getProtocolVersion() {
			
			return null;
		}

		@Override
		public void setProtocolVersion(HttpVersion version) {
			

		}

		@Override
		public ChannelBuffer getContent() {
			
			return null;
		}

		@Override
		public void setContent(ChannelBuffer content) {
			

		}

		private boolean headerSet;
		@Override
		public void addHeader(String name, Object value) {
			assertEquals(WebLogicSsl.WL_PROXY_SSL_HEADER, name);
			assertEquals(WebLogicSsl.WL_PROXY_SSL_HEADER_VALUE, value);
			headerSet = true;
		};
		
		public boolean isHeaderSet(){
			return headerSet;		
		}
		@Override
		public void setHeader(String name, Object value) {
			

		}

		@Override
		public void setHeader(String name, Iterable<?> values) {
			

		}

		@Override
		public void removeHeader(String name) {
			

		}

		@Override
		public void clearHeaders() {
			

		}

		@Override
		public long getContentLength() {
			
			return 0;
		}

		@Override
		public long getContentLength(long defaultValue) {
			
			return 0;
		}

		@Override
		public boolean isChunked() {
			
			return false;
		}

		@Override
		public void setChunked(boolean chunked) {
			

		}

		@Override
		public boolean isKeepAlive() {
			
			return false;
		}

		@Override
		public HttpMethod getMethod() {
			
			return null;
		}

		@Override
		public void setMethod(HttpMethod method) {
			

		}

		@Override
		public String getUri() {
			
			return null;
		}

		@Override
		public void setUri(String uri) {
			

		}

	}


}
