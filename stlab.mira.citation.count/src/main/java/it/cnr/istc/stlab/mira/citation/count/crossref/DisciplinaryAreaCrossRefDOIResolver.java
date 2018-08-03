package it.cnr.istc.stlab.mira.citation.count.crossref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
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

import it.cnr.istc.stlab.mira.commons.DOIResolver;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class DisciplinaryAreaCrossRefDOIResolver {

	
	public static final String DISCIPLINARY_AREA_OPTION = "d";
	public static final String DISCIPLINARY_AREA_OPTIONS_LONG = "discipline";
	
	public static final String QUALIFICATION_LEVEL_OPTION = "l";
	public static final String QUALIFICATION_LEVEL_OPTIONS_LONG = "level";
	
	public static final String OUTPUT_OPTION = "o";
	public static final String OUTPUT_OPTIONS_LONG = "output";
	
	private DOIResolver doiResolver;
	
	public DisciplinaryAreaCrossRefDOIResolver() {
		this.doiResolver = new CrossRefDOIResolver();
	}
	
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
        	
        	System.out.println("Level: " + qualificationLevel + " - Area: " + disciplinaryAreaCode);
        	File rdfQlOutFolder = new File(outFolder, qualificationLevel);
        	File rdfDaOutFolder = new File(rdfQlOutFolder, disciplinaryAreaCode);
        	
        	rdfDaOutFolder.mkdirs();
        	
        	if(qualificationLevel != null && disciplinaryAreaCode != null){
        		
        		DisciplinaryAreaCrossRefDOIResolver doiResolver = new DisciplinaryAreaCrossRefDOIResolver();
        		File qualificationLevelFolder = new File(sessionFolderPath, qualificationLevel);
        		if(qualificationLevelFolder.exists() && qualificationLevelFolder.isDirectory()){
        			File disciplinaryAreaFolder = new File(qualificationLevelFolder, disciplinaryAreaCode);
        			if(disciplinaryAreaFolder.exists() && disciplinaryAreaFolder.isDirectory()){
        				File[] rdfFiles = disciplinaryAreaFolder.listFiles(f->f.getName().endsWith(".ttl"));
        				List<File> files = new ArrayList<File>();
        				Collections.addAll(files, rdfFiles);
        				
        				files.forEach(rdfFile -> {
        					System.out.println(rdfFile.getName());
        					
        					RDFParser parser = new TurtleParserFactory().getParser();
        					Model model = new LinkedHashModel();
        					StatementCollector collector = new StatementCollector(model);
        					parser.setRDFHandler(collector);
        					
        					try {
								parser.parse(new FileInputStream(rdfFile), QualificationProcessOntology.NS);
								
								model = doiResolver.addMissingDOIs(model);
								
								OutputStream out = new FileOutputStream(new File(rdfDaOutFolder, rdfFile.getName()));
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
								for(Statement stmt : model)
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
	
	
	public List<Resource>  findPublicationsWithoutDoi(File rdfFile){
		
		List<Resource> publications = new ArrayList<Resource>();
		Model outModel = new LinkedHashModel();
		
		RDFParser parser = new TurtleParserFactory().getParser();
		Model model = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(model);
		parser.setRDFHandler(collector);
		
		try {
			parser.parse(new FileInputStream(rdfFile), QualificationProcessOntology.NS);
			
			publications = findPublicationsWithoutDoi(model);		
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return publications;
		
	}
	
	public List<Resource>  findPublicationsWithoutDoi(Model model){
		
		List<Resource> publications = new ArrayList<Resource>();
		
		model.filter(null, QualificationProcessOntology.hasPublications, null).
			objects()
			.forEach(publicationsList -> {
				model.filter((URI)publicationsList, QualificationProcessOntology.hasPart, null)
					.objects()
					.parallelStream()
					.forEach(biblioEntry -> {
						model.filter((URI)biblioEntry, QualificationProcessOntology.refers, null)
						.objects()
						.forEach(publication -> {
							if(publication != null){
								Set<Value> dois = model.filter((URI)publication, QualificationProcessOntology.doi, null).objects();
								
								if(dois == null || dois.isEmpty() || model.filter((URI)publication, QualificationProcessOntology.hasCitationCount, null).isEmpty()){
									publications.add((URI)publication);
								}
							}
						});
					});
			});
				
				
			
					
		return publications;
		
	}
	
	public Model addMissingDOIs(Model model){
		
		List<PublicationWithDoi> publicationWithDOIs = resolveDOIs(model);
		System.out.println('\t' + "Added " + publicationWithDOIs.size() + " missing DOIs.");
		resolveDOIs(model)
			.forEach(publicationWithDOI -> {
				if(publicationWithDOI.doi != null){
					model.remove(publicationWithDOI.publication, QualificationProcessOntology.doi, null);
					model.add(publicationWithDOI.publication, QualificationProcessOntology.doi, new LiteralImpl(publicationWithDOI.doi));
				}
			});
		
		return model;
		
	}
	
	public List<PublicationWithDoi> resolveDOIs(Model model){
		
		List<PublicationWithDoi> publicationsWithDoi = findPublicationsWithoutDoi(model).parallelStream()
				.map(publication -> {
					String doi = null;
					try{
						Model bilioRecordModel = model.filter(publication, null, null);
						doi = doiResolver.resolve(bilioRecordModel);
					} catch(Throwable e) {
						System.out.println("CICCIO");
						System.out.println("+++ " + publication);	
					}
					return new PublicationWithDoi((URI)publication, doi);
				})
				.collect(Collectors.toList());
		
		return publicationsWithDoi;
	}
	
	public class PublicationWithDoi {
		URI publication;
		String doi;
		
		public PublicationWithDoi(URI publication, String doi) {
			this.publication = publication;
			this.doi = doi;
		}
		
		public URI getPublication() {
			return publication;
		}
		
		public String getDoi() {
			return doi;
		}
		
		
	}
}
