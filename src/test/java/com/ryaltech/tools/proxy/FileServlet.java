package com.ryaltech.tools.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class FileServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getOutputStream().println(req.getAttribute("file").toString());
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {	

		String fileName = (String) request.getParameter("file");
        FileInputStream fis = null;
        OutputStream out = null;

        try {

            fis = new FileInputStream(fileName);
            response.setContentType("application/octet-stream"); 

            out = response.getOutputStream();
            IOUtils.copy(fis, out);  
                                    

        } finally {            

            IOUtils.closeQuietly(out); 
            IOUtils.closeQuietly(fis);

        }
	}
	

}
