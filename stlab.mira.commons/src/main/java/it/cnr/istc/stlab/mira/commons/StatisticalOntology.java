package it.cnr.istc.stlab.mira.commons;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class StatisticalOntology {

	/** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://w3id.org/anvur/nq/statistics/";
    
    /** <p>The namespace of the vocabulary as a URI</p> */
    public static final URI NAMESPACE = new URIImpl( NS );
    
    public static final URI correlation = new URIImpl( NS + "correlation" );
    
    public static final URI pValue = new URIImpl( "http://w3id.org/anvur/nq/ontology/pValue" );
    
    public static final URI hasExaminedMetrics = new URIImpl( "http://w3id.org/anvur/nq/ontology/hasExaminedMetrics" );
    
    public static final URI level = new URIImpl( "http://w3id.org/anvur/nq/ontology/level" );
    
    public static final URI PearsonCorrelation = new URIImpl( "http://w3id.org/anvur/nq/ontology/PearsonCorrelation" );
    
    
}
