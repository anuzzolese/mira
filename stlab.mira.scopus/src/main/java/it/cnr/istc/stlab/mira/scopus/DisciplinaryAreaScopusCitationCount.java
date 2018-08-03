package it.cnr.istc.stlab.mira.scopus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
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

import it.cnr.istc.stlab.mira.commons.PublicationWithDoi;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.ciatationmodel.MetricsJSONSchema;

public class DisciplinaryAreaScopusCitationCount {

	
	public static final String DISCIPLINARY_AREA_OPTION = "d";
	public static final String DISCIPLINARY_AREA_OPTIONS_LONG = "discipline";
	
	public static final String APPEND_OPTION = "a";
	public static final String APPEND_OPTIONS_LONG = "append";
	
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
                .required(false)
                .longOpt(DISCIPLINARY_AREA_OPTIONS_LONG)
                .build();
        
        optionBuilder = Option.builder(QUALIFICATION_LEVEL_OPTION);
        Option qualificationLevelOption = optionBuilder.argName("string")
                .desc("The level of qualification. Valid levels are 1 and 2")
                .hasArg()
                .required(true)
                .longOpt(QUALIFICATION_LEVEL_OPTIONS_LONG)
                .build();
        
        optionBuilder = Option.builder(APPEND_OPTION);
        Option appendOption = optionBuilder.argName("append")
                .desc("If append mode is declared then already available outputs are not overwritten.")
                .required(false)
                .longOpt(APPEND_OPTION)
                .build();
        
        options.addOption(outputOption);
        options.addOption(disciplinaryAreaOption);
        options.addOption(qualificationLevelOption);
        options.addOption(appendOption);
		
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
        	
        	boolean append = commandLine.hasOption(APPEND_OPTION);
        	
        	String disciplinaryAreaCode = null;
        	if(commandLine.hasOption(DISCIPLINARY_AREA_OPTION)){
				String value = commandLine.getOptionValue(DISCIPLINARY_AREA_OPTION);
				if(value != null) disciplinaryAreaCode = value.trim();
			}
        	
        	File rdfOutFolder = null;
        	File jsonOutFolder = null;
        	if(commandLine.hasOption(OUTPUT_OPTION)){
				String value = commandLine.getOptionValue(OUTPUT_OPTION);
				if(value != null) {
					File outFolder = new File(value.trim());
					outFolder.mkdirs();
					
					rdfOutFolder = new File(outFolder, "rdf");
					jsonOutFolder = new File(outFolder, "json");
				}
			}
        	
        	System.out.println("Level: " + qualificationLevel + " - Area: " + disciplinaryAreaCode);
        	
        	DataCollector dataCollector = new DataCollector();
        	
        	if(qualificationLevel != null){
        		
        		File rdfQlOutFolder = new File(rdfOutFolder, qualificationLevel);
            	File jsonQlOutFolder = new File(jsonOutFolder, qualificationLevel);
            	
        		File qualificationLevelFolder = new File(sessionFolderPath, qualificationLevel);
        		File rdfFolder = new File(qualificationLevelFolder, "rdf");
    			File jsonFolder = new File(qualificationLevelFolder, "json");
        		if(disciplinaryAreaCode != null){	
        			File rdfDisciplinaryAreaFolder = new File(rdfFolder, disciplinaryAreaCode);
        			File jsonDisciplinaryAreaFolder = new File(jsonFolder, disciplinaryAreaCode);
        			
        			File rdfDaOutFolder = new File(rdfQlOutFolder, disciplinaryAreaCode);
        			File jsonDaOutFolder = new File(jsonQlOutFolder, disciplinaryAreaCode);
        			rdfDaOutFolder.mkdirs();
                	jsonDaOutFolder.mkdirs();
        			
        			handleDiscipline(append, dataCollector, rdfDisciplinaryAreaFolder, jsonDisciplinaryAreaFolder, rdfDaOutFolder, jsonDaOutFolder);
        		}
        		else{
        			File[] disciplinaryAreaFolders = rdfFolder.listFiles(f -> {return f.isDirectory() && !f.isHidden();});
        			for(File disciplinaryAreaFolder : disciplinaryAreaFolders){
        				
            			File jsonDisciplinaryAreaFolder = new File(jsonFolder, disciplinaryAreaFolder.getName());
            			
            			File rdfDaOutFolder = new File(rdfQlOutFolder, disciplinaryAreaFolder.getName());
            			File jsonDaOutFolder = new File(jsonQlOutFolder, disciplinaryAreaFolder.getName());
            			rdfDaOutFolder.mkdirs();
                    	jsonDaOutFolder.mkdirs();
            			
            			handleDiscipline(append, dataCollector, disciplinaryAreaFolder, jsonDisciplinaryAreaFolder, rdfDaOutFolder, jsonDaOutFolder);
        			}
        		}
        	}
		}	
		
    
		
		
	}
	
	private static void handleDiscipline(boolean append, DataCollector dataCollector, File rdfDisciplinaryFolder, File jsonDisciplinaryFolder, File outRdf, File outJson){
		if(rdfDisciplinaryFolder.exists() 
				&& rdfDisciplinaryFolder.isDirectory()
				&& jsonDisciplinaryFolder.exists() 
				&& jsonDisciplinaryFolder.isDirectory()){
			
			
			File[] rdfFiles = rdfDisciplinaryFolder.listFiles(f->f.getName().endsWith(".ttl"));
			List<File> files = new ArrayList<File>();
			Collections.addAll(files, rdfFiles);
			
			files.forEach(rdfFile -> {
				System.out.println(rdfFile.getName());
				Model outModel = new LinkedHashModel();
				
				
				File outRDFFile = new File(outRdf, rdfFile.getName());
				File outJSONFile = new File(outJson, rdfFile.getName().replace(".ttl", ".json"));
				
				boolean execute = true;
				if(append){
					if(outRDFFile.exists() && outJSONFile.exists())
						execute = false;
				}
				
				if(execute){
					JSONObject globalJson = new JSONObject();
					RDFParser parser = new TurtleParserFactory().getParser();
					Model model = new LinkedHashModel();
					StatementCollector collector = new StatementCollector(model);
					parser.setRDFHandler(collector);
					try {
						parser.parse(new FileInputStream(rdfFile), QualificationProcessOntology.NS);
						
						List<PublicationWithDoi> pubs = dataCollector.collect(model);
						
						pubs.forEach(pub -> {
							if(pub != null){
								JSONObject object = CitationModel.generateJSONObject(pub);
								Model modelTmp = MetricsJSONSchema.asRDF(pub.getPublication(), object);
								outModel.addAll(modelTmp);
								
								Iterator<String> keys = object.keys();
								keys.forEachRemaining(key -> {
									try {
										globalJson.put((String)key, object.get((String)key));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								});
							}
							
						});
						
						outModel.addAll(model);
						
						File jsonFile = new File(jsonDisciplinaryFolder, rdfFile.getName().replace(".ttl", ".json"));
						StringBuilder jsonContentSB = new StringBuilder();
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), "UTF-8"));
						String line = null;
						while((line = reader.readLine()) != null){
							if(jsonContentSB.length() > 0) jsonContentSB.append('\n');
							jsonContentSB.append(line);
						}
						reader.close();
						
						JSONObject previousJSON = null;
						try {
							previousJSON = new JSONObject(jsonContentSB.toString());
							Iterator<String> keys = previousJSON.keys();
							while(keys.hasNext()){
								String key = keys.next();
								
								globalJson.put((String)key, previousJSON.get((String)key));
								
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						
						OutputStream out = new FileOutputStream(outRDFFile);
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
						
						
						BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outJSONFile));
						try {
							bufferedWriter.write(globalJson.toString(4));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bufferedWriter.close();
						
					} catch (RDFParseException | RDFHandlerException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	
}
