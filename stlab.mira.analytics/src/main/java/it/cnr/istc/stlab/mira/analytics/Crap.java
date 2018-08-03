package it.cnr.istc.stlab.mira.analytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVWriter;

public class Crap {

	public static void main(String[] args) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File("/Users/andrea/Desktop/evaluationsetting.txt")));
			String line = null;
			
			Map<String, Sentence> sentenceMap = new HashMap<String, Sentence>();
			String sentenceId = null;
			while((line = reader.readLine()) != null){
				
				line = line.trim();
				if(line.startsWith("SENTENCE_")){
					sentenceId = line.substring(0, line.length()-1);
					sentenceMap.put(sentenceId, new Crap().new Sentence(sentenceId));
				}
				else if(line.startsWith("QUESTION:")){
					String question = line.replaceAll("^QUESTION: ", "");
					question = question.replaceAll("\t", " ");
					question = question.trim();
					
					Sentence sentence = sentenceMap.get(sentenceId);
					sentence.addQuestion(question);
					
				}
				else if(line.startsWith("TEXT:")){
					String text = line.replaceAll("^TEXT: ", "");
					text = text.trim();
					text = text.substring(1, text.length()-1);	
					
					Sentence sentence = sentenceMap.get(sentenceId);
					sentence.setText(text);
				}
			}
			
			CSVWriter writer = new CSVWriter(new FileWriter(new File("/Users/andrea/Desktop/evaluationsetting.csv")));
			writer.writeNext(new String[]{"Sentence ID", "Sentence", "Question"});
			List<String> ids = sentenceMap.keySet().stream().sorted((a,b)->{
				Integer idA = Integer.valueOf(a.replace("SENTENCE_", ""));
				Integer idB = Integer.valueOf(b.replace("SENTENCE_", ""));
				if(idA == idB) return 0;
				else if(idA > idB) return 1;
				else return -1;
			}).collect(Collectors.toList());
			
			for(String id : ids){
				Sentence sentence = sentenceMap.get(id);
				for(String question : sentence.questions){
					writer.writeNext(new String[]{sentence.id, sentence.text, question});
				}
			}
			
			writer.close();
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(reader != null){
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class Sentence {
		String id, text;
		List<String> questions;
		
		public Sentence(String id){
			this.id = id;
			this.questions = new ArrayList<String>();
		}
		
		public String getId() {
			return id;
		}
		
		public String getText() {
			return text;
		}
		
		public List<String> getQuestions() {
			return questions;
		}
		
		public void setText(String text) {
			this.text = text;
		}
				
		public void addQuestion(String question){
			this.questions.add(question);
		}
	}
}
