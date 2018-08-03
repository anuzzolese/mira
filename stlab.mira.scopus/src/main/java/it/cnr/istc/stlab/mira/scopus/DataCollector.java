package it.cnr.istc.stlab.mira.scopus;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.vocabulary.RDF;

import it.cnr.istc.stlab.mira.commons.PublicationWithDoi;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class DataCollector {

	
	public List<PublicationWithDoi> collect(Model model){
		URI publicationList = model.filter(null, QualificationProcessOntology.hasPublications, null).objectURI();
		
		List<Statement> publications = model.filter(publicationList, QualificationProcessOntology.hasPart, null)
			.parallelStream()
			.map(stmt -> {
				URI publication = model.filter((URI)stmt.getObject(), QualificationProcessOntology.refers, null).objectURI();
				return new StatementImpl(publication, RDF.TYPE, QualificationProcessOntology.Publication);
			})
			.collect(Collectors.toList());
		//Model publications = model.filter(null, RDF.TYPE, QualificationProcessOntology.Publication);
		
		return publications.parallelStream()
			.filter(stmt -> {
				Model citationCountModel = model.filter(stmt.getSubject(), QualificationProcessOntology.hasCitationCount, null);
				if(citationCountModel.isEmpty()){
					if(model.contains(stmt.getSubject(), QualificationProcessOntology.doi, null))
						return true;
					else return false;
				}
				else{
					boolean[] ret = new boolean[]{true};
					citationCountModel.forEach(ccStmt -> {
						URI cc = (URI)ccStmt.getObject();
						Model citationTypeModel = model.filter(cc, 
								QualificationProcessOntology.hasCitationCountType, 
								new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/citations"));
						if(!citationTypeModel.isEmpty()){
							Set<Value> citationIndexesObjects = model.filter(cc, 
									QualificationProcessOntology.hasPart, 
									null).objects();
							
							Iterator<Value> citationIndexesObjectsIt = citationIndexesObjects.iterator();
							boolean stop = false;
							while(citationIndexesObjectsIt.hasNext() && !stop){
								URI citationIndexes = (URI) citationIndexesObjectsIt.next();
								
								if(model.contains(citationIndexes, 
										QualificationProcessOntology.hasCitationCountType, 
										new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/citation-indexes"))){
									
									Set<Value> concreteCitationIndexes = model.filter(citationIndexes, 
											QualificationProcessOntology.hasPart, 
											null).objects();
									concreteCitationIndexes.forEach(citationIndex ->{
										if(model.contains((URI)citationIndex, 
												QualificationProcessOntology.hasCitationCountType, 
												new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/scopus"))){
											ret[0] = false;
										}
									});
									
									stop = true;

								}
							}
							
						
						}
						
					});
					return ret[0];
				}
				
				
			})
			.map(stmt -> {
				try{
					Literal doi = model.filter(stmt.getSubject(), QualificationProcessOntology.doi, null).objectLiteral();
					return new PublicationWithDoi((URI)stmt.getSubject(), doi);
				} catch(ModelException e){
					return null;
				}
			})
			.collect(Collectors.toList());
	}
}
