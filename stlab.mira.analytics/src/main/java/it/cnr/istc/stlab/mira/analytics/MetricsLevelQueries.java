package it.cnr.istc.stlab.mira.analytics;

import org.openrdf.model.vocabulary.RDFS;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class MetricsLevelQueries {

	public static final String LEVEL_1 = "SELECT DISTINCT ?publication ?doi ?citationCountType ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi . "
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
			+ "}";
	
	public static final String LEVEL_1_BY_YEAR = "SELECT DISTINCT ?publication ?doi ?citationCountType ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi; "
			+ "    <" + QualificationProcessOntology.year + "> $1 . "
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
			+ "}";
	
	public static final String LEVEL_2 = "SELECT DISTINCT ?publication ?doi ?citationCountType ?ctLabel ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi . "
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
			+ "}";
	
	public static final String LEVEL_2_BY_YEAR = "SELECT DISTINCT ?publication ?doi ?citationCountType ?ctLabel ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi; "
			+ "    <" + QualificationProcessOntology.year + "> $1 . "
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
			+ "}";
	
	public static final String LEVEL_3 = "SELECT DISTINCT ?publication ?doi ?citationCountType ?ctLabel2 ?ctLabel ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi . "
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
			+ "}";
	
	public static final String LEVEL_3_BY_YEAR = "SELECT DISTINCT ?publication ?doi ?citationCountType ?ctLabel2 ?ctLabel ?label ?score "
			+ "WHERE{ "
			+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
			+ "    <" + QualificationProcessOntology.doi + "> ?doi; "
			+ "    <" + QualificationProcessOntology.year + "> $1 . "
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
			+ "}";
	
	public static final String LEVEL_1_YEAR_BASED = "PREFIX qpo: <http://w3id.org/anvur/nq/ontology/> "
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "SELECT DISTINCT ?publication ?year ?doi ?citationCountType ?label ?score "
			+ "WHERE{ "
			+ "  ?publication qpo:hasCitationCount  ?citationCount; "
			+ "    qpo:doi  ?doi;"
			+ "    qpo:year ?year"
			+ "  { "
			+ "    ?citationCount qpo:hasCitationCountType  ?citationCountType;"
			+ "      qpo:scoreValue  ?score ."
			+ "    ?citationCountType rdfs:label  ?label ."
			+ "    FILTER(?citationCountType != <http://w3id.org/anvur/nq/data/citation-count-type/citations>)"
			+ "  } "
			+ "  UNION {"
			+ "    ?citationCount qpo:hasCitationCountType  ?citationCountType;"
			+ "      qpo:scoreValue  ?score . "
			+ "    ?citationCountType rdfs:label  ?label . "
			+ "    FILTER(?citationCountType = <http://w3id.org/anvur/nq/data/citation-count-type/citations>)"
			+ "  }"
			+ "}";
	
}
