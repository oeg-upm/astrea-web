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
import org.apache.jena.riot.RiotException;
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
import oeg.validation.astrea.service.exceptions.SyntaxOntologyException;
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
	        @ApiResponse(code = 206, message = "There was an error processing one of the ontology formats from the URLs provided, some partial results could be output"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/shacl/url", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}, consumes= {"application/json"}) 
	@ResponseBody
	public String shapesFromOwlURL(@ApiParam(value = "A json document with ontology URLs and their formats.", required = true ) @Valid @RequestBody(required = true) Endpoints ontologyURLs, HttpServletResponse response) throws Exception {
		prepareResponse(response);
		log.info("Requested: "+ontologyURLs.getOntologies());
		String shapes = null;
		try {
			Model ontologies = loadOntologies(ontologyURLs.getOntologies(), response);
			List<String> additionalOntologyURLs = ontologies.listObjectsOfProperty(OWL.imports).toList().stream().map(url -> url.toString()).collect(Collectors.toList());
			Model importedOntologies = loadOntologies(additionalOntologyURLs, response);
			ontologies.add(importedOntologies);
			Model shapesAux = astreaService.generateShacl(ontologies);	
			System.out.println(">"+response.getStatus());
			if(shapesAux.isEmpty()) {
				response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT);
				shapes = modelToString(shapesAux);
				log.severe("shapes produced are empty");
			}
		}catch(SyntaxOntologyException e) {
			shapes = e.getMessage();
		}catch(Exception e) {
			log.severe(e.toString());
		}
		log.info("Requested solved.");
		return shapes;
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
		String shapes =  null;
		log.info("Requested ontology content ");
		Model ontologyModel = ModelFactory.createDefaultModel();
		try {
			InputStream is = new ByteArrayInputStream(ontology.getOntology().getBytes() );
			ontologyModel.read(is, null, ontology.getSerialisation());
			List<String> additionalOntologyURLs = ontologyModel.listObjectsOfProperty(OWL.imports).toList().stream().map(url -> url.toString()).collect(Collectors.toList());
			Model importedOntologies = loadOntologies(additionalOntologyURLs, response);
			ontologyModel.add(importedOntologies);
			Model shapesAux = astreaService.generateShacl(ontologyModel);		
			if(shapesAux.isEmpty()) {
				response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
				shapes = modelToString(shapesAux);
				log.severe("shapes produced are empty");
			}
			log.info("Requested solved.");
		}catch(RiotException e) {
			shapes = e.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return shapes;
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
	
	private Model loadOntologies(List<String> ontologyURLs, HttpServletResponse response) throws Exception {
		Model ontology = ModelFactory.createDefaultModel();
		response.setStatus( HttpServletResponse.SC_OK );
		if(ontologyURLs!=null && !ontologyURLs.isEmpty()) {
			for(int index=0; index < ontologyURLs.size(); index++) {
				String url = ontologyURLs.get(index).trim();
				try {
					Model ontologyTemporal = ModelFactory.createDefaultModel();
					String jenaFormat = computeFormat(url);
					if(jenaFormat!=null) {
						ontologyTemporal.read(url,jenaFormat);
					}else {
						ontologyTemporal.read(url);
					}
					if(!ontologyTemporal.isEmpty())
						ontology.add(ontologyTemporal);
				}catch(RiotException e) {
					response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT );
					throw new SyntaxOntologyException(e.toString());
				}catch(Exception e) {
					response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT );
					log.severe(e.toString());
				}
			}
		} 
		return ontology;
	}


	private String computeFormat(String url) {
		String format = url.substring(url.lastIndexOf('.'));
		if(format!=null && !format.isEmpty()) {
			if(format.endsWith(".ttl")){
	            format="Turtle";
	        }else if(format.endsWith(".nt")){
	            format="N-Triples";
	        }else if(format.endsWith(".nq")){
	            format="N-Quads";
	        }else if(format.endsWith(".trig")){
	            format="TriG";
	        }else if(format.endsWith(".rdf")){
	            format="RDF/XML";
	        }else if(format.endsWith(".owl")){
	            format="RDF/XML";
	        }else if(format.endsWith(".jsonld")){
	            format="JSON-LD";
	        }else if(format.endsWith(".trdf")){
	            format="RDF Thrift";
	        }else if(format.endsWith(".rt")){
	            format="RDF Thrift";
	        }else if(format.endsWith(".rj")){
	            format="RDF/JSON";
	        }else if(format.endsWith(".trix")){
	            format="TriX";
	        }else {
	        		format = null;
	        }
		}else {
			format = null;
		}
		return format;
	}
		
	
}
