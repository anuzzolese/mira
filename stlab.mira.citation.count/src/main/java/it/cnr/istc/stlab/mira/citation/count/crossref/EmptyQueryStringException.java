package it.cnr.istc.stlab.mira.citation.count.crossref;

public class EmptyQueryStringException extends Exception {
		
		@Override
		public String getMessage() {
			return "The query string is empty.";
		}
	}