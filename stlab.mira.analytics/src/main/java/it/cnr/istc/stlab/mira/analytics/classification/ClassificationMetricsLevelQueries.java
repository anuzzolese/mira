package it.cnr.istc.stlab.mira.analytics.classification;

import org.openrdf.model.vocabulary.RDFS;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class ClassificationMetricsLevelQueries {

	public static final String LEVEL_1 = "SELECT DISTINCT ?publication ?doi ?year ?citationCountType ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi; "
			+ "    <" + QualificationProcessOntology.year + "> ?year . "
			+ "{"
			+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> ?citationCountType;"
			+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score . "
			+ "?citationCountType <" + RDFS.LABEL + "> ?label . "
			+ "FILTER(?citationCountType != <http://w3id.org/anvur/nq/data/citation-count-type/citations>)" 
			+ "}"
			+ "UNION {"
			+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> ?citationCountType; "
			+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score . "
			+ "?citationCountType <" + RDFS.LABEL + "> ?label . "
			+ "FILTER(?citationCountType = <http://w3id.org/anvur/nq/data/citation-count-type/citations>)"
			+ "}" + '\n'
			+ "} "
			+ "ORDER BY ?doi";
	
	public static final String LEVEL_2 = "SELECT DISTINCT ?publication ?doi ?year ?citationCountType ?ctLabel ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi; "
			+ "    <" + QualificationProcessOntology.year + "> ?year . "
			+ "{"
			+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> ?ct;" + '\n'
			+ "    <" + QualificationProcessOntology.hasPart + "> ?part . "
			+ " ?part <" + QualificationProcessOntology.hasCitationCountType + "> ?citationCountType;"
			+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score . " + '\n'
			+ "?citationCountType <" + RDFS.LABEL + "> ?label . "
			+ "?ct <" + RDFS.LABEL + "> ?ctLabel . "
			+ "FILTER(?ct != <http://w3id.org/anvur/nq/data/citation-count-type/citations>)" 
			+ "}"
			+ "UNION {"
			+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> ?citationCountType; "
			+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score . "
			+ "?citationCountType <" + RDFS.LABEL + "> ?label . "
			+ "BIND(\"Citations\" AS ?ctLabel)"
			+ "FILTER(?citationCountType = <http://w3id.org/anvur/nq/data/citation-count-type/citations>)"
			+ "}" + '\n'
			+ "} "
			+ "ORDER BY ?doi";
	
	public static final String LEVEL_3 = "SELECT DISTINCT ?publication ?doi ?year ?citationCountType ?ctLabel2 ?ctLabel ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi; "
			+ "    <" + QualificationProcessOntology.year + "> ?year . "
			+ "{"
			+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> ?ct;" + '\n'
			+ "    <" + QualificationProcessOntology.hasPart + "> ?part2 . "
			+ " ?part2 <" + QualificationProcessOntology.hasCitationCountType + "> ?ct2;"
			+ "    <" + QualificationProcessOntology.hasPart + "> ?part . "
			+ " ?part <" + QualificationProcessOntology.hasCitationCountType + "> ?citationCountType;"
			+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score . " + '\n'
			+ "?citationCountType <" + RDFS.LABEL + "> ?label . "
			+ "?ct <" + RDFS.LABEL + "> ?ctLabel . "
			+ "?ct2 <" + RDFS.LABEL + "> ?ctLabel2 . "
			+ "FILTER(?ct != <http://w3id.org/anvur/nq/data/citation-count-type/citations>)" 
			+ "}"
			+ "UNION {"
			+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> ?citationCountType; "
			+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score . "
			+ "?citationCountType <" + RDFS.LABEL + "> ?label . "
			+ "BIND(\"Citations\" AS ?ctLabel)"
			+ "FILTER(?citationCountType = <http://w3id.org/anvur/nq/data/citation-count-type/citations>)"
			+ "}" + '\n'
			+ "} "
			+ "ORDER BY ?doi";
	
}
