package util;

import ga.GlobalVars;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import data.Event;
import data.TemporaryData;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class FileOperations {
	
	public static void clearAllFiles() throws IOException{
		Writer output = null;
		
		File file = new File("../ITTC2Simplified/OutputFiles/InitialPopulation.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		output.close();
		
		file = new File("../ITTC2Simplified/OutputFiles/popPStats.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		output.close();
		
		file = new File("../ITTC2Simplified/OutputFiles/popPenaltyValues.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		output.close();
		
		
		file = new File("../ITTC2Simplified/OutputFiles/popAvgDiversity.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		output.close();

		file = new File("../ITTC2Simplified/OutputFiles/popRStats.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		file = new File("../ITTC2Simplified/OutputFiles/popRobustnessValues.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		file = new File("../ITTC2Simplified/OutputFiles/popSecondRobustnessValues.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		file = new File("../ITTC2Simplified/OutputFiles/FinalPopulation.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		file = new File("../ITTC2Simplified/OutputFiles/BestIndividualFound.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		file = new File("../ITTC2Simplified/OutputFiles/individualText.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		file = new File("../ITTC2Simplified/OutputFiles/ParetoFront.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		file = new File("../ITTC2Simplified/OutputFiles/RunDetails.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.close();
		
	}
	
	public static void writeRunDetails(List<String> data) throws IOException{
		
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/RunDetails.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		for(String row: data){
			output.write(row + System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	
	
	public static void writeBestIndividualFoundToTxt(Individual bestIndiv) throws IOException{
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/BestIndividualFound.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int courseId;
		Event evt; 	
		int counter=0;
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		output.write("Individual:"+ counter+ System.getProperty( "line.separator" ));
		for (int i=0; i< bestIndiv.Data.length; i++){
			courseId= convertionManager.intToCourseId(bestIndiv.Data[i]);
			evt= convertionManager.intToEvent(i, bestIndiv.Data[i]);
			output.write("Course:"+ courseId+ ":"+evt.hours+ ":"+evt.time+ ":"+evt.room+ System.getProperty( "line.separator" ));
		} // end i for
		counter++;

		output.close();
	}
	
	public static void writeFinalPopToTxt(Individual[] indivs) throws IOException{
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/FinalPopulation.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		int courseId;
		Event evt; 	
		int counter=0;
		for (Individual ind: indivs){
			output.write("Individual:"+ counter+ System.getProperty( "line.separator" ));
			for (int i=0; i< ind.Data.length; i++){
				courseId= convertionManager.intToCourseId(ind.Data[i]);
				evt= convertionManager.intToEvent(i, ind.Data[i]);
				output.write("Course:"+ courseId+ ":"+evt.hours+ ":"+evt.time+ ":"+evt.room+ System.getProperty( "line.separator" ));
			} // end i for
			counter++;
		} // end ind for
		output.close();
	}
	
	public static void writePopPenaltyStats(List<int[]> numData) throws IOException{
		
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/popPStats.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		for(int[] row: numData){
			for (int i:row )
				output.write(i + "\t\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writePopPenaltyValues(List<int[]> penalties) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/popPenaltyValues.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		for(int[] row: penalties){
			for (int i:row )
				output.write(i + "\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writePopAvgDiversity(List<Float> numData) throws IOException{
		
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/popAvgDiversity.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("***A new run***"+ System.getProperty( "line.separator"));
		
		for(float row: numData){
			output.write(row + "\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writeDiversityRelationToFile(float[][] indIndTotalDiff) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/diversityRelations.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int row =0 ; row < indIndTotalDiff.length; ++row){
			 for(int column =0; column<indIndTotalDiff[row].length;++column)
				 output.write(indIndTotalDiff[row][column] + "\t");
			 output.write(System.getProperty( "line.separator"));
		}
		
		output.close();
		
	}
	
	public static void writePopRobustnessStats(List<Double> numData) throws IOException{
		
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/popRStats.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		for (double i:numData ){
			output.write(i + "\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writePopRobustnessValues(List<double[]> popRStats) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/popRobustnessValues.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		for(int row=0; row< popRStats.size(); row++){
			for (double i: popRStats.get(row) )
				output.write(i + "\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writePopSecondRobustnessValues(List<double[]> popRStats) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/popSecondRobustnessValues.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		for(int row=0; row< popRStats.size(); row++){
			for (double i: popRStats.get(row) )
				output.write(i + "\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writeToFile(ArrayList<Integer> penaltyAtEachTemperature) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/penaltyAtEachTofSA.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int p: penaltyAtEachTemperature){
			output.write(p + "\t");
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}

	
	
	public static void printFinalSolutionToText(Individual ind) throws IOException{
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/individualText.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		int courseId;
		Event evt;
		output.write(System.getProperty( "line.separator"));
		for(int i=0; i< ind.Data.length; i++){
			if (ind.Data[i]==0)
				continue;
			courseId= convertionManager.intToCourseId(ind.Data[i]);
			evt= convertionManager.intToEvent(i, ind.Data[i]);
			for (int l=0; l<evt.hours; l++){
				output.write(TemporaryData.courseCode[courseId]+ "\t");
				if (evt.room!= parameters.UNUSED_ROOM)
					output.write(TemporaryData.roomCode[evt.room]+ "\t");
				output.write(dataHolder.timeslotDays[evt.time+l]+ "\t");
				output.write(((evt.time+l)%parameters.numDailyPeriods)+ "\t");
				output.write(System.getProperty( "line.separator"));
			}
		}
		output.close();			
	}
	
	public static void writeInitialPopToTxt(Individual[] indivs) throws IOException{
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/InitialPopulation.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));
		
		int courseId;
		Event evt; 	
		int counter=0;
		
		for (Individual ind: indivs){
			output.write("Individual:"+ counter+ System.getProperty( "line.separator" ));
			for (int i=0; i< ind.Data.length; i++){
				courseId= convertionManager.intToCourseId(ind.Data[i]);
				evt= convertionManager.intToEvent(i, ind.Data[i]);
				output.write("Course:"+ courseId+ ":"+evt.hours+ ":"+evt.time+ ":"+evt.room+ System.getProperty( "line.separator" ));
			} // end i for
			counter++;
		} // end ind for
		output.close();
	}	
	
	public static void getInitialSolutionFromTxt(Individual[] indivs) throws IOException{
		// Get from this array: 
		File file = new File("../ITTC2Simplified/OutputFiles/InitialPopulation.txt");
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		int indIndex = 0; int courseId = 0; int hours = 0; int time = 0; int room = 0;
		String[] temp;
		String line;
		while ((line = br.readLine()) != null) {
		   temp= line.split("\\:");
		   if (temp[0].compareTo("Individual")==0){
			   indIndex= Integer.parseInt(temp[1].trim());
			   if (indIndex >= PopulationParameters.populationSize ){
				   br.close();
				   return;
			   }
			   for (int i=0; i< indivs[indIndex].Data.length; i++){
				   line= br.readLine();
				   temp= line.split("\\:");
				   courseId= Integer.parseInt(temp[1].trim());
				   hours= Integer.parseInt(temp[2].trim());
				   time= Integer.parseInt(temp[3].trim());
				   room= Integer.parseInt(temp[4].trim());
				   indivs[indIndex].Data[i]= convertionManager.eventValuesToInt(courseId, hours, time, room);  
			   }
		   }
		} // end line loop
		br.close();
	}
	
	public static void getBestIndividualFromTxt(Individual indiv) throws IOException{
		// Get from this array: 
		File file = new File("../ITTC2Simplified/OutputFiles/BestIndividualFound.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		int indIndex = 0; int courseId = 0; int hours = 0; int time = 0; int room = 0;
		String[] temp;
		String line;
		while ((line = br.readLine()) != null) {
		   temp= line.split("\\:");
		   if (temp[0].compareTo("Individual")==0){
			   indIndex= Integer.parseInt(temp[1].trim());
			   if (indIndex >= PopulationParameters.populationSize ){
				   br.close();
				   return;
			   }
			   for (int i=0; i< indiv.Data.length; i++){
				   line= br.readLine();
				   temp= line.split("\\:");
				   courseId= Integer.parseInt(temp[1].trim());
				   hours= Integer.parseInt(temp[2].trim());
				   time= Integer.parseInt(temp[3].trim());
				   room= Integer.parseInt(temp[4].trim());
				   indiv.Data[i]= convertionManager.eventValuesToInt(courseId, hours, time, room);  
			   }
		   }
		} // end line loop
		br.close();
	}
	
	public static void getInitialSolutionFromFinalPopTxt(Individual[] indivs) throws IOException{
		// Get from this array: 
		File file = new File("../ITTC2Simplified/OutputFiles/FinalPopulation.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		int indIndex = 0; int courseId = 0; int hours = 0; int time = 0; int room = 0;
		String[] temp;
		String line;
		while ((line = br.readLine()) != null) {
		   temp= line.split("\\:");
		   if (temp[0].compareTo("Individual")==0){
			   indIndex= Integer.parseInt(temp[1].trim());
			   if (indIndex >= PopulationParameters.populationSize ){
				   br.close();
				   return;
			   }
			   
			   for (int i=0; i< indivs[indIndex].Data.length; i++){
				   line= br.readLine();
				   temp= line.split("\\:");
				   courseId= Integer.parseInt(temp[1].trim());
				   hours= Integer.parseInt(temp[2].trim());
				   time= Integer.parseInt(temp[3].trim());
				   room= Integer.parseInt(temp[4].trim());
				   indivs[indIndex].Data[i]= convertionManager.eventValuesToInt(courseId, hours, time, room);  
			   }
		   }
		} // end line loop
		br.close();
	}
	
	public static void printFinalSolutionAnalysis(List<String> anal) throws IOException{
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/finalSolutonAnalysis.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, false )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(String s: anal){
			output.write(s);
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}
	
	public static void writeInitializationDetails(List<String> res) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/initDetails.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(String s: res){
			output.write(s);
			output.write(System.getProperty( "line.separator"));
		}
		output.close();
	}

	
//	public static void printFinalSolutionToSheet(Individual ind) throws IOException, RowsExceededException, WriteException{
//	
//		WritableWorkbook workbook = Workbook.createWorkbook(new File("../ITTC2Simplified/OutputFiles/timetables.xls"));
//		WritableFont redFont = new WritableFont(WritableFont.ARIAL);
//	    redFont.setColour(Colour.BLUE2);
//	    WritableCellFormat cf = new WritableCellFormat(redFont);
//	    cf.setBackground(Colour.YELLOW);
//	    cf.setAlignment(Alignment.CENTRE);
//		    
//	    WritableSheet sheet = workbook.createSheet("curriculumsAG", 1);
//	    
//		Event evt; 
//		String curCode;
//		String courseCode;
//		String context;
//		int rowCounter= 0;
//		
//		for (int cur=0; cur< parameters.numCurriculums; cur++){
//			curCode= TemporaryData.curriculumCode[cur];
//			context= "Curriculum "+ curCode+ " - Courses:"+ TemporaryData.curriculum_CourseCount[cur]; 
//			Label lbl = new Label(0, rowCounter, context, cf);
//			sheet.addCell(lbl);
//			rowCounter++;
//			lbl = new Label(1, rowCounter, "Day1", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(2, rowCounter, "Day2", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(3, rowCounter, "Day3", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(4, rowCounter, "Day4", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(5, rowCounter, "Day5", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(6, rowCounter, "Day6", cf);
//			sheet.addCell(lbl);
//			
//			rowCounter++;
//			lbl = new Label(0, rowCounter, "Timeslot1", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(0, rowCounter+1, "Timeslot2", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(0, rowCounter+2, "Timeslot3", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(0, rowCounter+3, "Timeslot4", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(0, rowCounter+4, "Timeslot5", cf);
//			sheet.addCell(lbl);
//			lbl = new Label(0, rowCounter+5, "Timeslot6", cf);
//			sheet.addCell(lbl);
//
//			for (int i=0; i< parameters.numEvents; i++){
//				evt= convertionManager.intToEvent(i, ind.Data[i]);
//				int courseId= dataHolder.eventCourseId[i];
//				if (dataHolder.course_Curriculum[courseId][cur]) {
//					courseCode= TemporaryData.courseCode[courseId];
//					int day= dataHolder.timeslotDays[evt.time];
//					int period= (evt.time % parameters.numDailyPeriods);
//					String temp= (sheet.getCell(day+1, rowCounter+period).getContents());
//					String cont= courseCode+ "\n" + "room:"+ TemporaryData.roomCode[evt.room];
//					if (temp.isEmpty()){						
//						lbl = new Label(day+1, rowCounter+period, cont);
//					}
//					else{
//						lbl = new Label(day+1, rowCounter+period, temp+" AND "+ cont);
//					}
//					sheet.addCell(lbl);
//				} // end if	
//			} // end i for
//			rowCounter= rowCounter+6;
//		} // end cur for
//		workbook.write();
//		workbook.close();
//
//	}

	
	public static void appendIndividualsToText(ArrayList<Individual> indivs) throws IOException{
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/indivFromOtherGAs.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int courseId;
		Event evt; 	
		int counter=0;
		for (Individual ind: indivs){
			output.write("Individual:"+ counter+ System.getProperty( "line.separator" ));
			for (int i=0; i< ind.Data.length; i++){
				courseId= convertionManager.intToCourseId(ind.Data[i]);
				evt= convertionManager.intToEvent(i, ind.Data[i]);
				output.write("Course:"+ courseId+ ":"+evt.hours+ ":"+evt.time+ ":"+evt.room+ System.getProperty( "line.separator" ));
			} // end i for
			counter++;
		} // end ind for
		output.close();
	}
	
	public static ArrayList<Individual> getIndividualsFromText() throws IOException{
		ArrayList<Individual> newIndividuals= new ArrayList<Individual>();
		Individual newInd;
		// Get from this file: 
		File file = new File("../ITTC2Simplified/OutputFiles/indivFromOtherGAs.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		@SuppressWarnings("unused")
		int indIndex = 0; int courseId = 0; int hours = 0; int time = 0; int room = 0;
		String[] temp;
		String line;
		while ((line = br.readLine()) != null) {
		   temp= line.split("\\:");
		   if (temp[0].compareTo("Individual")==0){
			   indIndex= Integer.parseInt(temp[1].trim());
			   newInd= new Individual();
			   for (int i=0; i< parameters.dataArraySize; i++){
				   line= br.readLine();
				   temp= line.split("\\:");
				   courseId= Integer.parseInt(temp[1].trim());
				   hours= Integer.parseInt(temp[2].trim());
				   time= Integer.parseInt(temp[3].trim());
				   room= Integer.parseInt(temp[4].trim());
				   newInd.Data[i]= convertionManager.eventValuesToInt(courseId, hours, time, room); 
			   }
			   newIndividuals.add(newInd);
		   } // end if
		} // end line loop
		br.close();
		return newIndividuals;
	}

	public static void printParetoToFile(Population pop) throws IOException {
		Writer output = null;
		File file = new File("../ITTC2Simplified/OutputFiles/ParetoFront.txt");
		try {
			output=  new BufferedWriter(new FileWriter(file, true )); // true: append mode.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.write(System.getProperty( "line.separator"));
		output.write("Run no: "+ GlobalVars.runCount+ System.getProperty( "line.separator"));

		for (int i=0; i< pop.individuals.length; i++){
			output.write(i+"\t"+pop.individuals[i].rank+"\t"+pop.individuals[i].crowdDistance+"\t\t"+pop.individuals[i].totalPenalty+"\t"+pop.individuals[i].robustValueMin);
			output.write(System.getProperty( "line.separator"));
		}
		output.write(System.getProperty( "line.separator"));
		output.close();
		
	}

	
	
	// get the individuals specific to a given run:
	public static void getInitialSolutionFromFinalPopTxt(Individual[] individuals, int runCount) throws NumberFormatException, IOException {
		// Get from this array: 
		File file = new File("../ITTC2Simplified/OutputFiles/FinalPopulation.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		int indIndex = 0; int courseId = 0; int hours = 0; int time = 0; int room = 0;
		String[] temp;
		String line;
		
		int indCounter= 0;
		
		while ((line = br.readLine()) != null) {
		   temp= line.split("\\s");
		   if (temp.length < 3) continue;
		   
		   if ((temp[0].trim()).compareTo("Run")==0){
			   if(Integer.parseInt((temp[2]).trim()) == runCount){ // at the right run count
				   while (indCounter <= individuals.length-1){
					   line = br.readLine();
//				
//						   (line = br.readLine()) != null) {
//					   
					   temp= line.split("\\:");
					   if ((temp[0].trim()).compareTo("Individual")==0){
						   indIndex= Integer.parseInt(temp[1].trim());
						   if (indIndex >= PopulationParameters.populationSize ){
							   br.close();
							   return;
						   }
						   for (int i=0; i< individuals[indIndex].Data.length; i++){
							   line= br.readLine();
							   temp= line.split("\\:");
							   courseId= Integer.parseInt(temp[1].trim());
							   hours= Integer.parseInt(temp[2].trim());
							   time= Integer.parseInt(temp[3].trim());
							   room= Integer.parseInt(temp[4].trim());
							   individuals[indIndex].Data[i]= convertionManager.eventValuesToInt(courseId, hours, time, room);  
						   } // end for
						   indCounter++;
					   } // end if
			
						   
				   } // end while
				   
				   // now no need to check for other run counts:
				   br.close();
				   return;
			   } // end if run count
		   } // end if
		} // end line loop
		br.close();
		
	}

	

}
