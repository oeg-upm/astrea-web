package oeg.validation.astrea.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A validation document has two URLs pointing one to a piece of RDF data and the other to a Shape document.")
public class ValidationURLDocument {

	@ApiModelProperty(notes = "A URL pointing to a piece of RDF data", required = true, position = 1, example="http://dbpedia.org/data/Madrid.ttl" )
	private String dataURL;
	@ApiModelProperty(notes = "A valid RDF format, currently supported: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX.", required = true, position = 1, example="Turtle" )
	private String dataFormat;
	@ApiModelProperty(notes = "A URL pointing to valid Shape document", required = true, position = 1, example="https://zenodo.org/record/4021024/files/DBpedia-SHACL-Shapes.ttl?download=1" )
	private String shapeURL;
	@ApiModelProperty(notes = "A valid RDF format, currently supported: Turtle, RDF/XML, N-Triples, JSON-LD, RDF/JSON, TriG, N-Quads, TriX.", required = true, position = 1, example="Turtle" )
	private String shapeFormat;
	@ApiModelProperty(notes = "This parameter specifies, if true, which types within the data are covered by the provided shape (cosider that those not cover by the shape are always correctly validated)", required = false, position = 1, example="true" )
	private boolean coverage;
	
	
	public ValidationURLDocument() {
		// empty
		dataURL = "";
		dataFormat = "";
		shapeURL = "";
		shapeFormat = "";
	}

	public String getDataURL() {
		return dataURL;
	}
	
	public void setDataURL(String data) {
		this.dataURL = data;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getShapeURL() {
		return shapeURL;
	}

	public void setShapeURL(String shape) {
		this.shapeURL = shape;
	}

	public String getShapeFormat() {
		return shapeFormat;
	}

	public void setShapeFormat(String shapeFormat) {
		this.shapeFormat = shapeFormat;
	}
	
	public boolean getCoverage() {
		return coverage;
	}

	public void setCoverage(boolean coverage) {
		this.coverage = coverage;
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("{");
		toString.append("\"data").append("\" : \"").append(dataURL).append("\",");
		toString.append("\"dataFormat").append("\" : \"").append(dataFormat).append("\",");
		toString.append("\"shape").append("\" : \"").append(shapeURL).append("\",");
		toString.append("\"shapeFormat").append("\" : \"").append(shapeFormat).append("\",");
		toString.append("}");
		return toString.toString();
	}


}
