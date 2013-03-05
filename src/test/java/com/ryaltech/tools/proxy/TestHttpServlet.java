package com.ryaltech.tools.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestHttpServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String signature;
	private RequestRecorder listener;
	
	public  TestHttpServlet(String signature, RequestRecorder listener) {
		this.listener = listener;
		this.signature = signature;

	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		if(listener!=null)listener.requestRecieved(req);
		resp.getOutputStream().println(signature);		
	}
	

}
