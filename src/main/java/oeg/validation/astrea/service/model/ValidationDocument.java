package oeg.validation.astrea.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A validation document has a piece of RDF data and a Shape document.")
public class ValidationDocument {

	@ApiModelProperty(notes = "A piece of RDF data", required = true, position = 1, example="this is a fuking example" )
	private String data;
	@ApiModelProperty(notes = "A valid RDF format", required = true, position = 1, example="Turtle" )
	private String dataFormat;
	@ApiModelProperty(notes = "A valid Shape document", required = true, position = 1, example="" )
	private String shape;
	@ApiModelProperty(notes = "A valid RDF format", required = true, position = 1, example="Turtle" )
	private String shapeFormat;
	
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
