package oeg.validation.astrea.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A document containing an ontology")
public class Ontology {

	
	@ApiModelProperty(notes = "An ontology document.", required = true, position = 1, example="@prefix : <http://www.w3.org/2006/time#> .\n" + 
			"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
			"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
			"@prefix skos: <http://www.w3.org/2004/02/skos/core#> .\n" + 
			"\n" + 
			":DateTimeInterval\n" + 
			"  rdf:type owl:Class ;\n" + 
			"  rdfs:comment \"DateTimeInterval is a subclass of ProperInterval, defined using the multi-element DateTimeDescription.\"@en ;\n" + 
			"  rdfs:label \"Date-time interval\"@en ;\n" + 
			"  rdfs:subClassOf :ProperInterval ;\n" + 
			"  skos:definition \"DateTimeInterval is a subclass of ProperInterval, defined using the multi-element DateTimeDescription.\"@en ;\n" + 
			"  skos:note \":DateTimeInterval can only be used for an interval whose limits coincide with a date-time element aligned to the calendar and timezone indicated. For example, while both have a duration of one day, the 24-hour interval beginning at midnight at the beginning of 8 May in Central Europe can be expressed as a :DateTimeInterval, but the 24-hour interval starting at 1:30pm cannot.\"@en ." )
	private String ontology;
	
	public Ontology() {
		// empty
		ontology = "";
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	@Override
	public String toString() {
		return "Ontology [ontology=" + ontology + "]";
	}


	

	
	
}

