package oeg.validation.astrea.service.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class WebController {


	@GetMapping("/")
	public String homepage(@RequestHeader Map<String, String> headers, HttpServletResponse response) {
		return "index.html";
	}
	
	@GetMapping("/index.html")
	public String homepageFromFile(@RequestHeader Map<String, String> headers, HttpServletResponse response) {
		return "index.html";
	}
}
