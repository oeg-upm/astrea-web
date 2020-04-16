package oeg.validation.astrea.service.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
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
import oeg.validation.astrea.service.model.OntologyDocument;
import oeg.validation.astrea.service.service.AstreaService;

@RestController
@Api(value="SHACL Shapes Builder")
public class GenerationAPI extends AbstractController{

	@Autowired
	public AstreaService astreaService;
	private Logger log = Logger.getLogger(GenerationAPI.class.getName());
	private static Map<String,String> sparqlResponseFormats;

	// -- POST method
	

	@ApiOperation(value = "Build SHACL shapes for the provided ontology URLs.")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Shapes successfully built"),
	        @ApiResponse(code = 206, message = "Some URLs provided built an empty shacl document"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/shacl/url", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}, consumes= {"application/json"}) 
	@ResponseBody
	public String shapesFromOwlURL(@ApiParam(value = "A json document with ontology URLs and their formats.", required = true ) @Valid @RequestBody(required = true) Endpoints ontologyURLs, HttpServletResponse response) {
		prepareResponse(response);
		log.info("Requested: "+ontologyURLs.getOntologies());
		Model ontologies = loadOntologies(ontologyURLs.getOntologies(), response);
		List<String> additionalOntologyURLs = ontologies.listObjectsOfProperty(OWL.imports).toList().stream().map(url -> url.toString()).collect(Collectors.toList());
		Model importedOntologies = loadOntologies(additionalOntologyURLs, response);
		ontologies.add(importedOntologies);
		Model shapes = astreaService.generateShacl(ontologies);		
		if(shapes.isEmpty()) {
			response.setStatus( HttpServletResponse.SC_NO_CONTENT );
			shapes=null;
			log.severe("shapes produced are empty");
		}
		log.info("Requested solved.");
		return modelToString(shapes);
	}
	
	
	@ApiOperation(value = "Build SHACL shapes from an ontology document, supported formats: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX.")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Shapes successfully built"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/shacl/document", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"} ) 
	@ResponseBody
	public String shapesFromOwlContent(@ApiParam( value = "A json specifying the ontology document and its format", required = true ) @Valid @RequestBody(required = true) OntologyDocument ontology, HttpServletResponse response, HttpServletRequest request) {
		prepareResponse(response);
		Model shapes =  ModelFactory.createDefaultModel();
		log.info("Requested ontology content ");
		Model ontologyModel = ModelFactory.createDefaultModel();
		try {
			InputStream is = new ByteArrayInputStream(ontology.getOntology().getBytes() );
			ontologyModel.read(is, null, ontology.getSerialisation());
			List<String> additionalOntologyURLs = ontologyModel.listObjectsOfProperty(OWL.imports).toList().stream().map(url -> url.toString()).collect(Collectors.toList());
			Model importedOntologies = loadOntologies(additionalOntologyURLs, response);
			ontologyModel.add(importedOntologies);
			shapes = astreaService.generateShacl(ontologyModel);		
			if(shapes.isEmpty()) {
				response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
				shapes=null;
				log.severe("shapes produced are empty");
			}
			log.info("Requested solved.");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return modelToString(shapes);
	}
	


	
	
	static{
		sparqlResponseFormats = new HashMap<>();
		sparqlResponseFormats.put("text/rdf+n3", "N-Triples" );
		sparqlResponseFormats.put("text/n3", "N-Triples" );
		sparqlResponseFormats.put("text/rdf+nt", "N-Triples");
		sparqlResponseFormats.put("text/ntriples", "N-Triples" );

		sparqlResponseFormats.put("text/rdf+ttl", "Turtle" );
		sparqlResponseFormats.put("text/rdf+turtle", "Turtle" );
		sparqlResponseFormats.put("text/turtle", "Turtle" );
		sparqlResponseFormats.put("application/turtle", "Turtle" );
		sparqlResponseFormats.put("application/x-turtle", "Turtle");
		sparqlResponseFormats.put("application/x-nice-turtle", "Turtle" );
		
		sparqlResponseFormats.put("application/x-trig", "TriG" );
		sparqlResponseFormats.put("application/rdf+xml", "RDF/XML");
		
		sparqlResponseFormats.put("application/ld+json", "JSON-LD" );
	}
	
	/*
	 * Auxiliary methods
	 */
	
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
				String url = ontologyURLs.get(index).trim();
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
