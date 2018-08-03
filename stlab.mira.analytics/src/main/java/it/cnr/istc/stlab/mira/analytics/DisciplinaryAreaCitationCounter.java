package it.cnr.istc.stlab.mira.analytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.openrdf.rio.turtle.TurtleWriterFactory;

import com.bigdata.rdf.internal.XSD;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders;

public class DisciplinaryAreaCitationCounter {

	
	public static final String DISCIPLINARY_AREA_OPTION = "d";
	public static final String DISCIPLINARY_AREA_OPTIONS_LONG = "discipline";
	
	public static final String QUALIFICATION_LEVEL_OPTION = "l";
	public static final String QUALIFICATION_LEVEL_OPTIONS_LONG = "level";
	
	public static final String OUTPUT_OPTION = "o";
	public static final String OUTPUT_OPTIONS_LONG = "output";
	
	public static void main(String[] args) {
		
		Options options = new Options();
		
		Builder optionBuilder = Option.builder(OUTPUT_OPTION);
        Option outputOption = optionBuilder.argName("folder")
                .desc("The name of the folder where to store the resulting RDF..")
                .hasArg()
                .required(true)
                .longOpt(OUTPUT_OPTIONS_LONG)
                .build();
        
        optionBuilder = Option.builder(DISCIPLINARY_AREA_OPTION);
        Option disciplinaryAreaOption = optionBuilder.argName("string")
                .desc("The code of the disciplinary area.")
                .hasArg()
                .required(true)
                .longOpt(DISCIPLINARY_AREA_OPTIONS_LONG)
                .build();
        
        optionBuilder = Option.builder(QUALIFICATION_LEVEL_OPTION);
        Option qualificationLevelOption = optionBuilder.argName("string")
                .desc("The level of qualification. Valid levels are 1 and 2")
                .hasArg()
                .required(true)
                .longOpt(QUALIFICATION_LEVEL_OPTIONS_LONG)
                .build();
        
        options.addOption(outputOption);
        options.addOption(disciplinaryAreaOption);
        options.addOption(qualificationLevelOption);
		
		CommandLine commandLine = null;
        
        CommandLineParser cmdLineParser = new DefaultParser();
        try {
            commandLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "citationCounter", options );
        }
        
		if(commandLine != null){
        	String[] arguments = commandLine.getArgs();
        	if(arguments.length != 1){
        		System.out.println("No correct arguments provided as input.");
        		System.exit(0);
        	}
        	
        	String sessionFolderPath = commandLine.getArgs()[0];
        	File sessionFolder = new File(sessionFolderPath);
        	if(!sessionFolder.exists() || !sessionFolder.isDirectory()){
        		System.out.println("The folder representing the session does not exist.");
        		System.exit(0);
        	}
        	
        	String qualificationLevel = null;
        	if(commandLine.hasOption(QUALIFICATION_LEVEL_OPTION)){
				String value = commandLine.getOptionValue(QUALIFICATION_LEVEL_OPTION);
				if(value != null){
					try{
						Integer lev = Integer.valueOf(value.trim());
						
						if(lev == 1) qualificationLevel = "fascia-1";
						else if(lev == 2) qualificationLevel = "fascia-2";
					} catch(NumberFormatException e){
						e.printStackTrace();
					}
					
				
				}
			}
        	
        	String disciplinaryAreaCode = null;
        	if(commandLine.hasOption(DISCIPLINARY_AREA_OPTION)){
				String value = commandLine.getOptionValue(DISCIPLINARY_AREA_OPTION);
				if(value != null) disciplinaryAreaCode = value.trim();
			}
        	
        	File outFolder = null;
        	if(commandLine.hasOption(OUTPUT_OPTION)){
				String value = commandLine.getOptionValue(OUTPUT_OPTION);
				if(value != null) {
					outFolder = new File(value.trim());
					outFolder.mkdirs();
				}
			}
        	
        	Model scopusModel = new LinkedHashModel();
        	URI scopusCitationCountType = new URIImpl(AnvurPDFHeaders.NS_DATA + "citation-count-type/scopus-citation-count");
        	scopusModel.add(scopusCitationCountType, RDF.TYPE, QualificationProcessOntology.CitationCountType);
        	scopusModel.add(scopusCitationCountType, RDFS.LABEL, new LiteralImpl("Scopus citation count"));
        	
        	URI scopusSource = new URIImpl(AnvurPDFHeaders.NS_DATA + "source/scopus-api");
        	scopusModel.add(scopusSource, RDF.TYPE, QualificationProcessOntology.Source);
        	scopusModel.add(scopusSource, RDFS.LABEL, new LiteralImpl("Scopus API"));
        	
        	URI citationCountUM= new URIImpl(AnvurPDFHeaders.NS_DATA + "unit-of-measure/citation-count");
        	scopusModel.add(citationCountUM, RDF.TYPE, QualificationProcessOntology.UnitOfMeasure);
        	scopusModel.add(citationCountUM, RDFS.LABEL, new LiteralImpl("Citation count unit of measure"));
        	
        	System.out.println("Level: " + qualificationLevel + " - Area: " + disciplinaryAreaCode);
        	File qlOutFolder = new File(outFolder, qualificationLevel);
        	File daOutFolder = new File(qlOutFolder, disciplinaryAreaCode);
        	
        	daOutFolder.mkdirs();
        	if(qualificationLevel != null && disciplinaryAreaCode != null){
        		
        		ScopusCitationCount scc = new ScopusCitationCount();
        		
        		File qualificationLevelFolder = new File(sessionFolderPath, qualificationLevel);
        		if(qualificationLevelFolder.exists() && qualificationLevelFolder.isDirectory()){
        			File disciplinaryAreaFolder = new File(qualificationLevelFolder, disciplinaryAreaCode);
        			if(disciplinaryAreaFolder.exists() && disciplinaryAreaFolder.isDirectory()){
        				File[] rdfFiles = disciplinaryAreaFolder.listFiles(f->f.getName().endsWith(".ttl"));
        				List<File> files = new ArrayList<File>();
        				Collections.addAll(files, rdfFiles);
        				
        				files.parallelStream().forEach(rdfFile -> {
        					System.out.println(rdfFile.getName());
        					Model outModel = new LinkedHashModel();
        					outModel.addAll(scopusModel);
        					
        					RDFParser parser = new TurtleParserFactory().getParser();
        					Model model = new LinkedHashModel();
        					StatementCollector collector = new StatementCollector(model);
        					parser.setRDFHandler(collector);
        					try {
								parser.parse(new FileInputStream(rdfFile), QualificationProcessOntology.NS);
								
								outModel.addAll(model);
								
								model.filter(null, QualificationProcessOntology.doi, null).forEach(stmt -> {
									
									URI publication = (URI)stmt.getSubject();
									
									String doi = stmt.getObject().stringValue();
									if(doi != null){
										doi = doi.replace("http://dx.doi.org/", "");
										int citationCount = scc.count(doi);
										
										if(citationCount >= 0){
										
											URI citationCountRes = new URIImpl(AnvurPDFHeaders.NS_DATA + "citation-count/" + publication.getLocalName());
											outModel.add(citationCountRes, RDF.TYPE, QualificationProcessOntology.CitationCount);
											outModel.add(citationCountRes, QualificationProcessOntology.hasCitationCountType, scopusCitationCountType);
											outModel.add(citationCountRes, QualificationProcessOntology.scoreValue, new LiteralImpl(String.valueOf(citationCount), XSD.INT));
											outModel.add(citationCountRes, QualificationProcessOntology.hasUnitOfMeasure, citationCountUM);
											
											outModel.add(publication, QualificationProcessOntology.hasCitationCount, citationCountRes);
										}
										
									}
								});;
								
								OutputStream out = new FileOutputStream(new File(daOutFolder, rdfFile.getName()));
								RDFWriter writer = new TurtleWriterFactory().getWriter(out);
								writer.handleNamespace("ref-list", "http://w3id.org/anvur/nq/data/reference-list/");
								writer.handleNamespace("biblio-item", "http://w3id.org/anvur/nq/data/bibliographic-item/");
								writer.handleNamespace("biblio-item-type", "http://w3id.org/anvur/nq/data/bibliographic-item-type/");
								writer.handleNamespace("application", "http://w3id.org/anvur/nq/data/application/");
								writer.handleNamespace("person", "http://w3id.org/anvur/nq/data/person/");
								writer.handleNamespace("resume", "http://w3id.org/anvur/nq/data/resume/");
								writer.handleNamespace("qualification-type", "http://w3id.org/anvur/nq/data/qualification-type/");
								writer.handleNamespace("qualification", "http://w3id.org/anvur/nq/data/qualification/");
								writer.handleNamespace("qualification-list", "http://w3id.org/anvur/nq/data/qualification-list/");
								writer.handleNamespace("publication", "http://w3id.org/anvur/nq/data/publication/");
								writer.handleNamespace("ont", "http://w3id.org/anvur/nq/ontology/");
								writer.handleNamespace("rdf", RDF.NAMESPACE);
								writer.handleNamespace("rdfs", RDFS.NAMESPACE);
								writer.handleNamespace("xsd", XSD.NAMESPACE);
								
								writer.startRDF();
								for(Statement stmt : outModel)
									writer.handleStatement(stmt);
								writer.endRDF();
								
								out.close();
								
							} catch (RDFParseException | RDFHandlerException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        				});
        			}
        		}
        	}
		}	
		
    
		
		
	}
}
