package oeg.validation.astrea.service.service;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;
import astrea.model.ShaclFromOwl;
import sharper.generators.OwlGenerator;

@Service
public class AstreaService {


	
	public Model generateShacl(String ontologyURL) {
		ShaclFromOwl shaclGenerator = new OwlGenerator();
		return shaclGenerator.fromURL(ontologyURL);
	}
	
	public Model generateShacl(Model ontologyModel) {
		ShaclFromOwl shaclGenerator = new OwlGenerator();
		return shaclGenerator.fromModel(ontologyModel);
	}
	
}
