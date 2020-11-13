package oeg.validation.astrea.service.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import oeg.validation.astrea.service.model.ValidationDocument;
import oeg.validation.astrea.service.model.ValidationURLDocument;
import oeg.validation.astrea.service.service.AstreaService;

@RestController
@Api(value="SHACL Shapes and RDF data validator")
public class ValidationAPI extends AbstractController{

	@Autowired
	public AstreaService astreaService;
	private Logger log = Logger.getLogger(ValidationAPI.class.getName());
	
	@ApiOperation(value = "Validate RDF data using a Shape document")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Validation performed correctly"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/validation/document", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}, consumes= {"application/json"}) 
	@ResponseBody
	public String validationWithShapeDocument(@ApiParam(value = "A document with RDF data, a Shape document, and the format used by both (it can be any of these: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX)", required = true ) @Valid @RequestBody(required = true) ValidationDocument document, HttpServletResponse response, HttpServletRequest request) {
		prepareResponse(response);
		Model report =  ModelFactory.createDefaultModel();
		log.info("Requested ontology content ");
		try {
			Model data = createModel(document.getData(), document.getDataFormat());
			Model shape = createModel(document.getShape(), document.getShapeFormat());
			Shapes shapes = Shapes.parse(shape);
			report = validate(shapes,  data);
			
			if(document.getCoverage()) {
				typesNotContainedInShape(data, shape, report);
				report.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
			}
			response.setStatus(200);
		}catch(Exception e) {
			e.printStackTrace();
		}
		log.info("Requested solved.");
		return modelToString(report);
	}
	
	@ApiOperation(value = "Validate RDF data online using a Shape document online")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Validation performed correctly"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/validation/url", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}, consumes= {"application/json"}) 
	@ResponseBody
	public String validationWithShapeURL(@ApiParam(value = "A document with a URL pointing to RDF data, a URL pointing to a Shape document, and the format used by both (it can be any of these: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX)", required = true ) @Valid @RequestBody(required = true) ValidationURLDocument document, HttpServletResponse response, HttpServletRequest request) {
		prepareResponse(response);
		Model report =  ModelFactory.createDefaultModel();
		log.info("Requested ontology content ");
		try {
			Model data = createModelFromURL(document.getDataURL(), document.getDataFormat());
			Model shape = createModelFromURL(document.getShapeURL(), document.getShapeFormat());
			if(data.isEmpty() || shape.isEmpty()) {
				response.setStatus(400);
			}else {
				Shapes shapes = Shapes.parse(shape);
				report = validate( shapes, data);
				if(document.getCoverage()) {
					typesNotContainedInShape(data, shape, report);
				}
				response.setStatus(200);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		log.info("Requested solved.");
		return modelToString(report);
	}
	
	@ApiOperation(value = "Validate RDF data using a Shape updating their files")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Validation performed correctly"),
	        @ApiResponse(code = 400, message = "Bad request")
	    })
	@RequestMapping(value = "/api/validation/file", method = RequestMethod.POST, produces = {"text/rdf+turtle", "text/turtle"}, consumes = {"multipart/form-data"}) 
	@ResponseBody
	public String validationWithShapeFile( @ApiParam(value = "Specifies if validation must output the coverage of the shape") @Valid @RequestParam("coverage") Boolean coverage, @ApiParam(value="A multipart/form-data file containing an ontology") @Valid @RequestParam("ontology_file") MultipartFile ontologyFile, @ApiParam(value = "Supported formats: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX") @Valid @RequestParam("ontology_format") String ontologyFormat, @ApiParam(value="A multipart/form-data file containing a SHACL shape")  @Valid @RequestParam("shape_file") MultipartFile shapeFile, @ApiParam(value = "Supported formats: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX") @Valid @RequestParam("shape_format") String shapeFormat, HttpServletResponse response, HttpServletRequest request) {
		prepareResponse(response);
		Model report =  ModelFactory.createDefaultModel();
		log.info("Requested ontology content ");
		try {
			String ontologyStr =  IOUtils.toString(ontologyFile.getInputStream(), StandardCharsets.UTF_8);
			String shapeStr =  IOUtils.toString(shapeFile.getInputStream(), StandardCharsets.UTF_8);
			Model data = createModel(ontologyStr, ontologyFormat);
			Model shape = createModel(shapeStr, shapeFormat);
			Shapes shapes = Shapes.parse(shape);
			report = validate(shapes,  data);
			
			if(coverage) {
				typesNotContainedInShape(data, shape, report);
				report.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
			}
			response.setStatus(200);
		}catch(Exception e) {
			e.printStackTrace();
		}
		log.info("Requested solved.");
		return modelToString(report);
	}
	
	public Model createModel(String rdf, String format) {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = new ByteArrayInputStream(rdf.getBytes() );
		model.read(is, null, format);  // jsonDocument.get("format")
		return model;
	}
	
	public Model createModelFromURL(String url, String format) {
		Model model = ModelFactory.createDefaultModel();
		model.read(url, null, format);  // jsonDocument.get("format")
		return model;
	}
	
	private Model validate(Shapes shapes, Model data) {
		Model report = ShaclValidator.get().validate(shapes, data.getGraph()).getModel();
		report.write(System.out);
		return report;
	}
	
	private String modelToString(Model model) {
		String content = null;
		if(model!=null) {
			Writer output = new StringWriter();
			model.write(output, "TTL");
			content = output.toString();
		}
		return content;
	}
	
	private void typesNotContainedInShape(Model data, Model shapes, Model report){
		NodeIterator iterator = data.listObjectsOfProperty(RDF.type);
		
		report.listSubjectsWithProperty(RDF.type, ResourceFactory.createResource("http://www.w3.org/ns/shacl#ValidationReport"))
		.toList()
		.forEach(elem -> report.add(elem, RDF.type, ResourceFactory.createResource("http://astrea.linkeddata.es/ontology#ValidationReport")));
		
		while(iterator.hasNext()) {
			RDFNode type = iterator.next();
			if(shapes.contains(null, null, type)) {
				report.listSubjectsWithProperty(RDF.type, ResourceFactory.createResource("http://www.w3.org/ns/shacl#ValidationReport"))
				.toList()
				.forEach(elem -> report.add(elem, ResourceFactory.createProperty("https://w3id.org/def/astrea#covers"), type));
			}else {
				report.listSubjectsWithProperty(RDF.type, ResourceFactory.createResource("http://www.w3.org/ns/shacl#ValidationReport"))
				.toList()
				.forEach(elem -> report.add(elem, ResourceFactory.createProperty("https://w3id.org/def/astrea#isAbsentTerm"), type));
			}
		}
		
	}
	
}
