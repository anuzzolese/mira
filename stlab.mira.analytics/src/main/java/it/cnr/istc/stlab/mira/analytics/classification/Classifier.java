package it.cnr.istc.stlab.mira.analytics.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Debug.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;

public class Classifier {
	 
	private final int RUNS = 10;
	private final int FOLDS = 10;
	private final String CLASS_ATTRIBUTE = "Result"; 
	
	public GlobalRates classify(File dataSourceFile, String...attributes){
		
		if(attributes==null || attributes.length==0) attributes = Attributes.FULL_ATTRIBUTES;
		
		GlobalRates globalRates = null;
		try {
			//Instances instances = ConverterUtils.DataSource.read(new FileInputStream(dataSourceFile));
			
			CSVLoader loader = new CSVLoader();
			loader.setSource(dataSourceFile);
			Instances instances = loader.getDataSet();
			
			Remove remove = new Remove();
			Enumeration<Attribute> attributesEnum = instances.enumerateAttributes();
			
			StringBuilder attributeIndexFilderSb = new StringBuilder();
			while(attributesEnum.hasMoreElements()){
				Attribute attribute = attributesEnum.nextElement();
				String attributeName = attribute.name();
				
				boolean found = false;
				for(int i=0; !found && i<attributes.length; i++){
					if(attributeName.equals(attributes[i]) || attributeName.equals(CLASS_ATTRIBUTE)) found = true;
				}
				if(!found){
					if(attributeIndexFilderSb.length() > 0) attributeIndexFilderSb.append(",");
					attributeIndexFilderSb.append(attribute.index()+1);
				}
			}
			
			remove.setAttributeIndices(attributeIndexFilderSb.toString());
			
			Rates trueRates = new Rates();
			Rates falseRates = new Rates();
			Rates averageRates = new Rates();
			
			for(int run=0; run<RUNS; run++){
				Random rand = new Random(run+1);
				Instances randData = new Instances(instances);
				randData.randomize(rand);
				
				
				
				
				Enumeration<Attribute> attrs = randData.enumerateAttributes();
				ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
				while(attrs.hasMoreElements()){
					Attribute attr = attrs.nextElement();
					attributeList.add(attr);
				}
				
				Instances balancedInstances = new Instances("t", attributeList, 1000);
				
				/*
				 * Add positive examples to the balanced dataset.
				 */
				randData
					.stream()
					.forEach(instance -> {
						String classValue = instance.stringValue(instances.attribute("Result")).trim();
						if(classValue.equals("true")){
							balancedInstances.add(instance);
						}
					});
				
				/*
				 * Add as many negative examples as available positive ones (in possible) to the balanced dataset.
				 */
				
				for(int i=0, k=0, j=balancedInstances.size(); k<j && k<randData.size() && i<randData.size(); i++){
					Instance instance = randData.get(i);
					String classValue = instance.stringValue(randData.attribute(CLASS_ATTRIBUTE)).trim();
					if(classValue.equals("false")){
						balancedInstances.add(instance);
						k++;
					}
				}
				
				//System.out.println(randData.size() + "-" + balancedInstances.size());
				
				rand = new Random(run+1);
				randData = new Instances(balancedInstances);
				randData.randomize(rand);

				randData.setClass(randData.attribute(CLASS_ATTRIBUTE));
				randData.stratify(FOLDS);
				
				/*
				Enumeration<Attribute> attributes = randData.enumerateAttributes();
				while(attributes.hasMoreElements()){
					System.out.println(attributes.nextElement().name());
				}
				*/
				
				for(int fold=0; fold<FOLDS; fold++){
					Instances train = randData.trainCV(FOLDS, fold);
					Instances test = randData.testCV(FOLDS, fold);
					
					
					
					train.setClass(train.attribute("Result"));
					test.setClass(train.attribute("Result"));
					
					//System.out.println("Train size: " + train.size());
					//System.out.println("Test size: " + test.size());
					
					//classifier.buildClassifier(train);
					
					FilteredClassifier filteredClassifier = new FilteredClassifier();
					filteredClassifier.setClassifier(new NaiveBayes());
					filteredClassifier.setFilter(remove);
					
					filteredClassifier.buildClassifier(train);
					
					Evaluation evaluation = new Evaluation(train);
					
					evaluation.evaluateModel(filteredClassifier, test);
					
					String classDetails = evaluation.toClassDetailsString();
					
					String processingClassDetails = classDetails.replace("=== Detailed Accuracy By Class ===", "").trim();
					processingClassDetails = processingClassDetails.replace("Weighted Avg.", "");
					processingClassDetails = processingClassDetails.replaceAll("(  )+", ";");
					
					StringBuilder stringBuilder = new StringBuilder();
					String[] lines = processingClassDetails.split("\n");
					
					for(int i=0; i<lines.length; i++){
						String line = lines[i];
						line = line.replaceAll("^;( )*", "");
						line = line.replaceAll(";( )*", ";");
						if(i>0) stringBuilder.append('\n');
						stringBuilder.append(line);
						
						String[] columns = line.split(";");
						if(i==0 && run==0){
							
							for(int j=0; j<columns.length-1; j++){
								String column = columns[j];
								trueRates.addRate(j, new Rate(column));
								falseRates.addRate(j, new Rate(column));
								averageRates.addRate(j, new Rate(column));
							}
						}
						else if(i>0){ 
							if(i<3){
								String classValue = columns[columns.length-1];
								if(classValue.equals("true")){
									for(int j=0; j<columns.length-1; j++){
										String column = columns[j];
										Rate rate = trueRates.getRate(j);
										rate.getScores().add(Double.valueOf(column));
									}
								}
								else{
									for(int j=0; j<columns.length-1; j++){
										String column = columns[j];
										Rate rate = falseRates.getRate(j);
										rate.getScores().add(Double.valueOf(column));
									}
								}
							}
							else {
								for(int j=0; j<columns.length; j++){
									String column = columns[j];
									Rate rate = averageRates.getRate(j);
									rate.getScores().add(Double.valueOf(column));
								}
							}
						}
						
					}
					
					//System.out.println(stringBuilder.toString());
					//System.out.println("Correct - " + correct);
					//System.out.println("Incorrect - " + incorrect);
					
					
					//System.out.println(confusionMatrix.length + "x" + confusionMatrix[0].length);
					//System.out.println("-------");
				}
			}
			
			globalRates = new GlobalRates(trueRates, falseRates, averageRates);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return globalRates;
	}
	
	public static void main(String[] args) {
		Classifier classifier = new Classifier();
		String inputFolder = "/Users/andrea/Desktop/anvur/sessione_1/step7-classification/input";
		String outputFolder = "/Users/andrea/Desktop/anvur/sessione_1/step7-classification/output_new";
		
		String[] fasce = {"fascia-1", "fascia-2"};
		String[] disciplinaryFields = {"01-B1", "04-A1", "06-N1", "09-H1", "13-A1"};
		
		//String[] experiments = {"full", "citations_only", "altmetrics_only", "citations_captures"};
		
		List<String> experiments = Attributes.getFeatureAttributes();
		
		for(String fascia : fasce){
			for(String disciplinaryField : disciplinaryFields){
				String inputFile = inputFolder + "/" + disciplinaryField + "_" + fascia + ".csv";
				
				
				for(String experiment : experiments){
					
					String[] attributes = Attributes.getFeaturesOf(experiment);
					/*
					String[] attributes = null;
					switch (experiment) {
					case "citations_only":
						attributes = Attributes.TRADITIONAL_METRICS_ATTRIBUTES;
						break;
					case "altmetrics_only":
						attributes = Attributes.ALT_METRICS_ATTRIBUTES;
						break; 
					case "citations_captures":
						attributes = Attributes.SELECTED_METRICS_ATTRIBUTES;
						break; 
					default:
						attributes = Attributes.FULL_ATTRIBUTES;
						break; 
					}
					*/
					String outputFile = outputFolder + "/" + disciplinaryField + "/" + fascia + "/" + experiment;
					
					GlobalRates globalRates = classifier.classify(new File(inputFile), attributes);
					globalRates.asCSVs(new File(outputFile));
				}
				
				
			}
		}
	}

}
