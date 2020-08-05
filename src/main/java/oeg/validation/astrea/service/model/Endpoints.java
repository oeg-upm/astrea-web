package oeg.validation.astrea.service.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "List of ontologies and their formats that are procesable by the application.")
public class Endpoints {
	
	@ApiModelProperty(notes = "List of online ontologies and their format.", required = true, position = 1, example="[\"https://www.w3.org/2006/time#\",\n \"https://albaizq.github.io/OpenADRontology/OnToology/ontology/openADRontology.owl/documentation/ontology.ttl\",\n \"http://xmlns.com/foaf/spec/index.rdf\",\n \"http://schema.org/version/latest/schema.jsonld\"\n]" )
	@NotEmpty
	private List<String> ontologies;
	
	public Endpoints() {
		// empty
		ontologies = new ArrayList<>();
	}

	public List<String> getOntologies() {
		return ontologies;
	}

	public void setOntologies(List<String> ontologies) {
		this.ontologies = ontologies;
	}


	@Override
	public String toString() {
		return  ontologies.toString();
	}

	
	
	
	
	
}
