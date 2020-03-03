package oeg.validation.astrea.service.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import oeg.validation.astrea.service.model.ValidationDocument;
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
	public String validationWithShape(@ApiParam(value = "A document with RDF data, a Shape document, and the format used by both (it can be any of these: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX)", required = true ) @Valid @RequestBody(required = true) ValidationDocument document, HttpServletResponse response, HttpServletRequest request) {
		prepareResponse(response);
		Model report =  ModelFactory.createDefaultModel();
		log.info("Requested ontology content ");
		try {
			Model data = createModel(document.getData(), document.getDataFormat());
			Model shape = createModel(document.getShape(), document.getShapeFormat());
			Shapes shapes = Shapes.parse(shape);
			report = ShaclValidator.get().validate(shapes.getGraph(), data.getGraph()).getModel();
			if(document.isStrict()) {
				typesNotContainedInShape(data, shape, report);
			}
			
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
	
	private String modelToString(Model model) {
		String content = null;
		if(model!=null) {
			Writer output = new StringWriter();
			model.write(output, "TURTLE");
			content = output.toString();
		}
		return content;
	}
	
	private void typesNotContainedInShape(Model data, Model shapes, Model report){
		NodeIterator iterator = data.listObjectsOfProperty(RDF.type);
		while(iterator.hasNext()) {
			RDFNode type = iterator.next();
			if(!shapes.contains(null, null, type)) {
				report.add(report.createResource(), ResourceFactory.createProperty("https://w3id.org/def/astrea##missingShapeFor"), type);
			}
		}
		
	}
	
}
