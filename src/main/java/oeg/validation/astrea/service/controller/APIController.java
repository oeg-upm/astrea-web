package oeg.validation.astrea.service.controller;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import oeg.validation.astrea.service.model.Endpoints;
import oeg.validation.astrea.service.service.AstreaService;

@RestController
@Api(value="SHACL Shapes Builder")
public class APIController extends AbstractController{

	@Autowired
	public AstreaService astreaService;
	private Logger log = Logger.getLogger(APIController.class.getName());

	// -- POST method
	

	@ApiOperation(value = "Build SHACL shapes for the provided ontology URLs.")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Shapes successfully built"),
	        @ApiResponse(code = 206, message = "Some URLs provided built an empty shacl document"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/shacl", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}) 
	@ResponseBody
	public String shapesFromOwlURL(@ApiParam(value = "A json document with ontology URLs and their formats. Accepted formats are: TURTLE, JSON-LD, NQ, NQUADS, NT, N-TRIPLES, RDF/XML, TriX", required = true ) @Valid @RequestBody(required = true) Endpoints ontologyURLs, HttpServletResponse response) {
		prepareResponse(response);
		Model ontologies = loadOntologies(ontologyURLs.getOntologies(), response);
		Model shapes = astreaService.generateShacl(ontologies);		
		if(shapes.isEmpty()) {
			response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
			shapes =null;
		}
		return modelToString(shapes);
	}
	
	
	private String modelToString(Model model) {
		String content = null;
		if(model!=null) {
			Writer output = new StringWriter();
			model.write(output, "TURTLE");
			content = output.toString();
		}
		return content;
	}
	
	private Model loadOntologies(List<String> ontologyURLs, HttpServletResponse response) {
		Model ontology = ModelFactory.createDefaultModel();
		response.setStatus( HttpServletResponse.SC_OK );
		if(ontologyURLs!=null && !ontologyURLs.isEmpty()) {
			for(int index=0; index < ontologyURLs.size(); index++) {
				String url = ontologyURLs.get(index);
				try {
					Model ontologyTemporal = ModelFactory.createDefaultModel();
					ontologyTemporal.read(url);
					if(!ontologyTemporal.isEmpty())
						ontology.add(ontologyTemporal);
				}catch(Exception e) {
					log.severe(e.toString());
					response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT );
				}
			}
		}
		return ontology;
	}
		
	
}
