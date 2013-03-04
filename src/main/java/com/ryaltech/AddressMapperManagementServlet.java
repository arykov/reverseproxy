package com.ryaltech;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddressMapperManagementServlet extends HttpServlet {
	private AddressMapper mapper;
	

	
	public AddressMapperManagementServlet(AddressMapper mapper) {
		super();
		this.mapper = mapper;
	}

	private String serializeToJson(Map<String, String>map){
		StringBuffer sb = new StringBuffer("{");
		boolean firstEntry = true;
		for(String key:map.keySet()){
			String value = map.get(key);
			if(value != null){
				if(!firstEntry)sb.append(',');
				sb.append('"').append(key).append("\":\"").append(value).append('"');
				firstEntry = false;				
			}
		}
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {		
		String str = serializeToJson(mapper.getMappings());
		resp.getWriter().println(str);		
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String path = req.getPathInfo();
		if( path == null)throw new AssertionError("Path should not be null.");
		String [] urls = path.replaceFirst("/", "").split("/(?=http://|https://)");
		if( urls.length != 2) throw new AssertionError("Two parameters expected http://from and http://to.");
		try{
			mapper.setAddressMapping(AddressMapper.fromUrl(urls[0]), AddressMapper.fromUrl(urls[1]));
		}catch(Exception ex){
			throw new RuntimeException("Parameters should be a valid url, starting with http:// or https://");
		}
		doGet(req, resp);
		
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String path = req.getPathInfo();
		if( path == null)throw new AssertionError("Path should not be null.");
		String [] urls = path.replaceFirst("/", "").split("/(?=http://|https://)");
		if( urls.length != 1) throw new AssertionError("Single parameter expected.");
		try{
			mapper.setAddressMapping(AddressMapper.fromUrl(urls[0]), null);
		}catch(Exception ex){
			throw new RuntimeException("Parameter should be a valid url, starting with http:// or https://");
		}
		doGet(req, resp);
		
	}


}
