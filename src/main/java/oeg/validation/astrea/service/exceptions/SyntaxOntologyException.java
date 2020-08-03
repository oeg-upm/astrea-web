package oeg.validation.astrea.service.exceptions;

public class SyntaxOntologyException extends Exception {

	private static final long serialVersionUID = 1L;

	public SyntaxOntologyException(String message) {
		super(message.replace("org.apache.jena.riot.RiotException: ", ""));
	}
}

