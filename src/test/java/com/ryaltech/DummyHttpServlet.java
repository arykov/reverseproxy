package com.ryaltech;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DummyHttpServlet extends HttpServlet {
	private String signature;
	public  DummyHttpServlet(String signature) {
		this.signature = signature;

	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		resp.getOutputStream().println(signature);		
	}
	

}
