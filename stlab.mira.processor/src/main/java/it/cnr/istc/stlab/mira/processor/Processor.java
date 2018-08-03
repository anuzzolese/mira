package it.cnr.istc.stlab.mira.processor;

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
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.altmetrics.AltmetricsModel;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class Processor {
	
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
        	
        	File qualificationLevelFolder = new File(outFolder, qualificationLevel);
        	File rdfQlOutFolder = new File(qualificationLevelFolder, "rdf");
        	File jsonQlOutFolder = new File(qualificationLevelFolder, "json");
        	
        	if(disciplinaryAreaCode != null){
        		handleDiscipline(sessionFolderPath, jsonQlOutFolder, rdfQlOutFolder, disciplinaryAreaCode, qualificationLevel);
        	}
        	else{
        		File qlFolder = new File(sessionFolderPath, qualificationLevel);
        		System.out.println(qualificationLevelFolder.getAbsolutePath());
        		File[] disciplineFolders = qlFolder.listFiles(disciplineFolder -> {return disciplineFolder.isDirectory() && !disciplineFolder.isHidden();});
        		for(File disciplineFolder : disciplineFolders){
        			System.out.println(disciplineFolder.getAbsolutePath());
        			handleDiscipline(sessionFolderPath, jsonQlOutFolder, rdfQlOutFolder, disciplineFolder.getName(), qualificationLevel);
        		}
        	}
		}
	}
	
	private static void handleDiscipline(String sessionFolderPath, File jsonQlOutFolder, File rdfQlOutFolder, String disciplinaryAreaCode, String qualificationLevel){
		
		System.out.println("Level: " + qualificationLevel + " - Area: " + disciplinaryAreaCode);
		
		File rdfDaOutFolder = new File(rdfQlOutFolder, disciplinaryAreaCode);
		File jsonDaOutFolder = new File(jsonQlOutFolder, disciplinaryAreaCode);
    	
    	rdfDaOutFolder.mkdirs();
    	jsonDaOutFolder.mkdirs();
    	
    	if(qualificationLevel != null && disciplinaryAreaCode != null){
    		
    		AltmetricsAdder altmetricsAdder = new AltmetricsAdder();
    		File qualificationLevelFolder = new File(sessionFolderPath, qualificationLevel);
    		if(qualificationLevelFolder.exists() && qualificationLevelFolder.isDirectory()){
    			File disciplinaryAreaFolder = new File(qualificationLevelFolder, disciplinaryAreaCode);
    			if(disciplinaryAreaFolder.exists() && disciplinaryAreaFolder.isDirectory()){
    				
    				System.out.println('\t' + disciplinaryAreaFolder.getName());
    				
    				File[] rdfFiles = disciplinaryAreaFolder.listFiles(f->f.getName().endsWith(".ttl"));
    				List<File> ttlsList = new ArrayList<File>();
    				Collections.addAll(ttlsList, rdfFiles);
    				
    				ttlsList.parallelStream()
    					.forEach(ttl -> {
    						try {
    							OutputStream rdfOut = new FileOutputStream(new File(rdfDaOutFolder, ttl.getName()));
	    						OutputStream jsonOut = new FileOutputStream(new File(jsonDaOutFolder, ttl.getName().replace(".ttl", ".json")));
	    						Model model = new LinkedHashModel();
	    						RDFParser parser = new TurtleParserFactory().getParser();
	    						StatementCollector collector = new StatementCollector(model);
	    						parser.setRDFHandler(collector);
    						
    							parser.parse(new FileInputStream(ttl), QualificationProcessOntology.NS);
    							AltmetricsModel altmetricsModel = altmetricsAdder.addAltmetrics(model);
    							
    							altmetricsModel.writeRDFModel(rdfOut);
    							altmetricsModel.writeJSONModel(jsonOut);
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
