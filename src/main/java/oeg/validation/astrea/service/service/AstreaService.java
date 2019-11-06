package oeg.validation.astrea.service.service;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

import astrea.model.ShaclFromOwl;
import sharper.generators.OptimisedOwlGenerator;

@Service
public class AstreaService {


	
	public Model generateShacl(String ontologyURL) {
		ShaclFromOwl shaclGenerator = new OptimisedOwlGenerator();
		return shaclGenerator.fromURL(ontologyURL);
	}
	
	public Model generateShacl(Model ontologyModel) {
		
		
		ShaclFromOwl shaclGenerator = new OptimisedOwlGenerator();
		return shaclGenerator.fromModel(ontologyModel);
	}
	
}
