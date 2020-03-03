package oeg.validation.astrea.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A validation document has a piece of RDF data and a Shape document.")
public class ValidationDocument {

	@ApiModelProperty(notes = "A piece of RDF data in any of these formats: ", required = true, position = 1, example="<http://example.org/ns#VEN1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/def/openadr#VEN> .\n" + 
			"<http://example.org/ns#VEN1> <https://w3id.org/def/openadr#receives> <https://w3id.org/def/openadr#Report091214_043741_028_0> .\n" + 
			"<http://example.org/ns#VEN1> <https://w3id.org/def/openadr#requests> <https://w3id.org/def/openadr#Report091214_043741_028_1> ." )
	private String data;
	@ApiModelProperty(notes = "A valid RDF format, currently supported: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX.", required = true, position = 1, example="N-Triples" )
	private String dataFormat;
	@ApiModelProperty(notes = "A valid Shape document", required = true, position = 1, example="prefix sh: <http://www.w3.org/ns/shacl#>\n"
			+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "prefix : <https://w3id.org/def/openadr#>\n"
			+ "<https://astrea.linkeddata.es/shapes#790eda9f1006c334de8cd96c41827613>\n" + 
			"      a                 sh:NodeShape ;\n" + 
			"        rdfs:isDefinedBy  \"OpenADR 2.0 Demand Response Program Implementation Guide\" ;\n" + 
			"        rdfs:label        \"Virtual End Node (VEN)\" ;\n" + 
			"        sh:description    \"This is the OpenADR Virtual End Node that is used to interact with the VTN\" ;\n" + 
			"        sh:name           \"Virtual End Node (VEN)\" ;\n" + 
			"        sh:nodeKind       sh:IRI ;\n" + 
			"        sh:property [\n" + 
			"		sh:path :receives;\n" + 
			"		sh:minCount 1 \n" + 
			"	];\n" + 
			"        sh:targetClass    :VEN ." )
	private String shape;
	@ApiModelProperty(notes = "A valid RDF format, currently supported: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX.", required = true, position = 1, example="Turtle" )
	private String shapeFormat;
	@ApiModelProperty(notes = "This parameter specifies, if true, which types within the data are not covered by the provided shape", required = false, position = 1, example="true" )
	private boolean strict;
	
	
	public ValidationDocument() {
		// empty
		data = "";
		dataFormat = "";
		shape = "";
		shapeFormat = "";
	}

	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getShapeFormat() {
		return shapeFormat;
	}

	public void setShapeFormat(String shapeFormat) {
		this.shapeFormat = shapeFormat;
	}
	
	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("{");
		toString.append("\"data").append("\" : \"").append(data).append("\",");
		toString.append("\"dataFormat").append("\" : \"").append(dataFormat).append("\",");
		toString.append("\"shape").append("\" : \"").append(shape).append("\",");
		toString.append("\"shapeFormat").append("\" : \"").append(shapeFormat).append("\",");
		toString.append("}");
		return toString.toString();
	}


}
