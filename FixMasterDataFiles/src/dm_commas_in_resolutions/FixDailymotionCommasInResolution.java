package dm_commas_in_resolutions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FixDailymotionCommasInResolution {

	public static void main(String[] args) throws IOException {		
		//New master data file 
		Path newFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + "test.txt");
		
		//Read in data from file
		Path oldFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
				"SummerResearch_DataCollection" + File.separator + "Dailymotion" + File.separator + "Dailymotion_MasterData.txt");
		List<String> oldLogEntries = Files.readAllLines(oldFilePath);
		
		//for each line
		for (String oldLogEntry : oldLogEntries) {
			//split by regex \t
			String[] splitLogEntry = oldLogEntry.split("\t");
			//split 4th String in array by ","
			String amendedResolutionsString = "";
			try {
				String[] splitResolutionsStringArray = (splitLogEntry[3]).split(",");
				//for each resolution
				//append resolution followed by ", "
				StringBuffer amendedResolutionsStringBuffer = new StringBuffer();
				for (int i = 0; i < splitResolutionsStringArray.length; i++) {
					amendedResolutionsStringBuffer.append(splitResolutionsStringArray[i] + ", ");
				}
				//remove the last ", "
				amendedResolutionsString = amendedResolutionsStringBuffer.substring(0, amendedResolutionsStringBuffer.length()-2);
			} catch (ArrayIndexOutOfBoundsException e) {
				//Nothing to amend, so do nothing.
			} finally {
				//piece the whole log entry string together
				StringBuffer amendedLogEntryStringBuffer = new StringBuffer();
				for (int j = 0; j < splitLogEntry.length-1; j++) {
					amendedLogEntryStringBuffer.append(splitLogEntry[j] + "\t");
				}
				amendedLogEntryStringBuffer.append(amendedResolutionsString + "\r\n");

				//add to list of strings
				String amendedLogEntryString = amendedLogEntryStringBuffer.toString();
//				modifiedLogEntries.add(amendedLogEntryString);
				System.out.print(amendedLogEntryString);
				Files.write(newFilePath, amendedLogEntryString.getBytes(), StandardOpenOption.APPEND);
				
			}
			
			
			
			
		}
			

		//FOr each string in list of strings
			//write to new master data file	
			
				
			
			
		
		
		

	}

}
