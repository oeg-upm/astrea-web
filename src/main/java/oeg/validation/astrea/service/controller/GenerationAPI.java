package oeg.validation.astrea.service.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.javatuples.Pair;
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
	        @ApiResponse(code = 206, message = "There was an error processing one of the ontology formats from the URLs provided, some partial results could be output"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/shacl/url", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}, consumes= {"application/json"}) 
	@ResponseBody
	public String shapesFromOwlURL(@ApiParam(value = "A json document with ontology URLs and their formats.", required = true ) @Valid @RequestBody(required = true) Endpoints ontologyURLs, HttpServletResponse response) throws Exception {
		prepareResponse(response);
		log.info("Requested ontologies URLs: "+ontologyURLs.getOntologies());
		Model shapes = ModelFactory.createDefaultModel();
		try {
			Pair<Model, Model> ontologies = loadOntologiesRecursivelly(ontologyURLs.getOntologies());
			shapes.add(astreaService.generateShacl(ontologies.getValue0()));	
			shapes.add(ontologies.getValue1());
			response.setStatus(compileResponseStatus(ontologies.getValue1()));
			shapes =  wrapShapes(shapes);
		}catch(Exception e) {
			log.severe(e.toString());
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
		log.info("Requested ontology content");
		
		try {
			Pair<Model,Model> ontologies = loadOntologiesFromContent(ontology.getOntology(),  ontology.getSerialisation());
			shapes.add(astreaService.generateShacl(ontologies.getValue0()));	
			shapes.add(ontologies.getValue1());
			response.setStatus(compileResponseStatus(ontologies.getValue1()));
			shapes =  wrapShapes(shapes);
		} catch(Exception e) {
			log.severe(e.toString());
		}
		log.info("Requested solved.");
		return modelToString(shapes);
	}
	
	private Model wrapShapes(Model shapes) {
		Model wrappedShapes = ModelFactory.createDefaultModel();
		wrappedShapes.add(shapes);
		Resource reportSubject = ResourceFactory.createResource("http://astrea.linkeddata.es/report");
		wrappedShapes.add(reportSubject, RDF.type, ResourceFactory.createResource("https://w3id.org/def/astrea#ShapeReport"));
		// add shapes
		List<String> subjects = extractSubjects(shapes,"http://www.w3.org/ns/shacl#NodeShape");
		subjects.addAll(extractSubjects(shapes,"http://www.w3.org/ns/shacl#PropertyShape"));
		subjects.forEach(subject -> wrappedShapes.add(reportSubject, ResourceFactory.createProperty("https://w3id.org/def/astrea#contains"), ResourceFactory.createResource(subject)));
		// add entries
		List<String> entries = extractSubjects(shapes,"https://w3id.org/def/astrea#ReportEntry");
		entries.forEach(entry -> wrappedShapes.add(reportSubject, ResourceFactory.createProperty("https://w3id.org/def/astrea#generatedShapesFrom"), ResourceFactory.createResource(entry)));
		return wrappedShapes;
	}
	
	private List<String> extractSubjects(Model dataModel, String clazz){
		List<String> subjects = new ArrayList<>();
		ResIterator nodeSubjects = dataModel.listSubjectsWithProperty(RDF.type, ResourceFactory.createResource(clazz));
		while(nodeSubjects.hasNext()) {
			Resource node = nodeSubjects.next();
			String nodeURI = node.getURI();
			if(nodeURI!=null) {
				subjects.add(nodeURI);
			}
		}
		return subjects;
	}
	
	
	private int compileResponseStatus(Model report) {
		NodeIterator statuses = report.listObjectsOfProperty(STATUS_CODE);
		int error = 0;
		int partial = 0;
		int ok = 0;
		while(statuses.hasNext()) {
			int status = statuses.next().asLiteral().getInt();
			if(status==200)
				ok++;
			if(status==206)
				partial++;
			if(status==400)
				error++;
		}
		
		if(partial > 1 || (ok>0 && error>0)) {
			return 206;
		}else if(partial==0 && error==0 && ok >0) {
			return 200;
		}else {
			return 400;
		}
		
		
	}
	
	private Pair<Model, Model> loadOntologiesRecursivelly(List<String> ontologyURLs) {
		Pair<Model, Model> ontologiesLoaded = loadOntologies(ontologyURLs);
		List<String> additionalOntologyURLs = ontologiesLoaded.getValue1().listObjectsOfProperty(OWL.imports).toList().stream().map(url -> url.toString()).collect(Collectors.toList());
		ontologiesLoaded.add(loadOntologies(additionalOntologyURLs));
		return ontologiesLoaded;
	}

	public static String captureStdOut(Runnable r)
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream out = System.out;
	    try {
	        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8.name()));
	        r.run();
	        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("End of the world, Java doesn't recognise UTF-8");
	    } finally {
	        System.setOut(out);
	    }
	}
	
	
	

	private Pair<Model,Model> loadOntologiesFromContent(String ontology, String lang) {
		Model ontologyModel = ModelFactory.createDefaultModel();	
		Model report = ModelFactory.createDefaultModel();
		String capturedLog = captureStdOut(() -> {
			try {
				InputStream is = new ByteArrayInputStream(ontology.getBytes() );
				ontologyModel.read(is, null, lang);
			}catch(Exception e) {
				// empty
			}
		});
		if(ontologyModel.contains(null, RDF.type, OWL.Ontology)) {
			report.add(buildReportDetailed("http://astrea.linkeddata.es/ontology#provided_ontology", capturedLog, ontologyModel.isEmpty(), false));
			List<String> additionalOntologyURLs = ontologyModel.listObjectsOfProperty(OWL.imports).toList().stream().map(url -> url.toString()).collect(Collectors.toList());
			Pair<Model, Model> ontologiesAux = loadOntologies(additionalOntologyURLs);
			ontologyModel.add(ontologiesAux.getValue0());
			report.add(ontologiesAux.getValue1());
		}else {
			report = buildReportDetailed("", "Provided content belongs to no ontology", true, false);
		}
		return Pair.with(ontologyModel, report);
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

	
	private Pair<Model,Model> loadOntologies(List<String> ontologyURLs) {
		Model ontology = ModelFactory.createDefaultModel();
		Model report = ModelFactory.createDefaultModel();
		for (int index = 0; index < ontologyURLs.size(); index++) {
			Model ontologyAux = ModelFactory.createDefaultModel();
			String url = ontologyURLs.get(index).trim();
			String generationLog = captureStdOut(() -> {
				try {
					Model ontologyTemporal = ModelFactory.createDefaultModel();
					String jenaFormat = computeFormat(url);
					if (jenaFormat != null) {
						ontologyTemporal.read(url, jenaFormat);
					} else {
						ontologyTemporal.read(url);
					}
					if (!ontologyTemporal.isEmpty())
						ontologyAux.add(ontologyTemporal);
				} catch (Exception e) {
					// empty
				}
			});
			if(ontologyAux.contains(null, RDF.type, OWL.Ontology)) {
				report.add(buildReport(url, generationLog, ontologyAux.isEmpty()));
				ontology.add(ontologyAux);
			}else {
				report.add(buildReportDetailed(url, "Provided content belongs to no ontology (maybe check that it contains the mandatory statement rdf:type owl:Ontology)", true, true));
			}
			
		}
		return Pair.with(ontology, report);
	}
	
	private static final Property STATUS_CODE = ResourceFactory.createProperty("https://w3id.org/def/astrea#statusCode");
	private static final Property MESSAGE = ResourceFactory.createProperty("https://w3id.org/def/astrea#message");
	private Model buildReport(String ontologyUrl, String message, Boolean emptyOntology) {
		return buildReportDetailed(ontologyUrl, message, emptyOntology, true);
	}
	
	private Model buildReportDetailed(String ontologyUrl, String message, Boolean emptyOntology, Boolean hasSource) {
		Model report = ModelFactory.createDefaultModel();
		String ontologyId = String.valueOf(ontologyUrl.hashCode()).replace("-", "0");
		Resource reportSubject = ResourceFactory.createResource("https://astrea.linkeddata.es/report/"+ontologyId);
		report.add(reportSubject, RDF.type, ResourceFactory.createResource("https://w3id.org/def/astrea#ReportEntry"));
		if(hasSource)
			report.add(reportSubject, ResourceFactory.createProperty("https://w3id.org/def/astrea#source"), ResourceFactory.createStringLiteral(ontologyUrl));
		if(message.isEmpty()) {
			report.add(reportSubject, STATUS_CODE, ResourceFactory.createTypedLiteral(200));
			report.add(reportSubject, MESSAGE, ResourceFactory.createStringLiteral("Shapes generated with no errors"));
		}else {
			report.add(reportSubject, MESSAGE, ResourceFactory.createStringLiteral(message.replaceAll("^?\n?.*org.apache.jena.riot\\s*:\\s*", "\n").replaceFirst("\n", "")));
			if(emptyOntology){
				report.add(reportSubject, STATUS_CODE, ResourceFactory.createTypedLiteral(400));
			}else{
				report.add(reportSubject, STATUS_CODE, ResourceFactory.createTypedLiteral(206));
			}
		}
		return report;
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
