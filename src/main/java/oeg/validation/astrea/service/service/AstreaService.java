package oeg.validation.astrea.service.service;

import org.apache.jena.rdf.model.Model;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import astrea.generators.OwlGenerator;

@Service
public class AstreaService {

	private OwlGenerator shaclGenerator;
	
	public AstreaService() {
		shaclGenerator = new OwlGenerator();
	}
	
	public Model generateShacl(String ontologyURL) {
		return shaclGenerator.fromURL(ontologyURL);
	}
	
	public Model generateShacl(Model ontologyModel) {
		return shaclGenerator.fromModel(ontologyModel);
	}
	
	
	@Scheduled(fixedDelay = 3600000)
	private void updateQueries() {
		shaclGenerator.fetchQueries();
	}
}
