package oeg.validation.astrea.service.controller;

import javax.servlet.http.HttpServletResponse;


public abstract class AbstractController {
	
	protected void prepareResponse(HttpServletResponse response) {
		response.setHeader("Server", "Astrea Service"); // Server type is hidden
		response.setStatus( HttpServletResponse.SC_OK ); // by default response code is OK
	}	
	
	
}
