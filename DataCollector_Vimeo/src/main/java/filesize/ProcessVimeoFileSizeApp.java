package filesize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mainpackage.MainApplicationVimeo;

public class ProcessVimeoFileSizeApp {

	public final static Path fileSizeMasterDataFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
			"SummerResearch_DataCollection" + File.separator + "Vimeo" + File.separator + "Vimeo_MasterFileSizeData.txt");

	public static void main(String[] args) throws IOException {
		System.out.println("------------START------------");

		ProcessVimeoFileSizeApp app = new ProcessVimeoFileSizeApp();

		Path masterDataFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + "testMaster.txt");
		//		Path masterDataFilePath = MainApplicationVimeo.masterDataFilePath; TODO

		//Read in data from master data file
		List<String> masterDataLogEntries = Files.readAllLines(masterDataFilePath);
		System.out.println("STATUS: Finished reading in data from master data file");

		//Get video IDs only from current file size master data file
		ArrayList<String> currentVideoIDsInFileSizeMaster = app.getCurrentVideoIDsInFileSizeMaster();
		System.out.println("STATUS: Finished getting video IDs only from current file size master data file");

		//Get only the video IDs that require finding file sizes
		ArrayList<String> videoIDsToProcess = app.getVideoIDsToProcess(masterDataLogEntries, currentVideoIDsInFileSizeMaster);
		System.out.println("STATUS: Finished getting only the video IDs that require finding file sizes");

		//Process the video IDs - get file size for each ID
		ArrayList<String> fileSizeMasterDataLogEntries = app.processForFileSize(videoIDsToProcess);
		System.out.println("STATUS: Finished processing the video IDs (getting the file sizes for each ID)");

		//Write new log entries to File Size Master Data file
		app.recordInFileSizeMasterDataFile(fileSizeMasterDataFilePath, fileSizeMasterDataLogEntries);
		System.out.println("STATUS: Finished recording log entries in File Size Master Data file");

	}

	public void recordInFileSizeMasterDataFile(Path fileSizeMasterDataFilePath, List<String> newLogEntries) {

		for (String newLogEntry : newLogEntries) {
			try {
				Files.write(fileSizeMasterDataFilePath, newLogEntry.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Given a list of preprocessed log entries (as a list of strings), 
	 * append additional info about the video's file size to each log entry, if there are info to garner.
	 * @param preprocessedLogEntries
	 * @return
	 */
	private ArrayList<String> processForFileSize(List<String> videoIDsToProcess) {

		ArrayList<String> fileSizeMasterDataLogEntries = new ArrayList<String>();

		for (String videoID : videoIDsToProcess) {
			StringBuffer tempSB = new StringBuffer(); //To be converted into a new log entry for File Size Master Data File later
			tempSB.append(videoID + "\t"); //First add video ID to new log entry

			String fileSizeOfFirstMostCommonResolution = this.findFileSize(videoID, "360p");
			tempSB.append(fileSizeOfFirstMostCommonResolution + "\t");

			String fileSizeOfSecondMostCommonResolution = this.findFileSize(videoID, "720p");
			tempSB.append(fileSizeOfSecondMostCommonResolution + "\r\n");

			String newLogEntry = tempSB.toString();
			fileSizeMasterDataLogEntries.add(newLogEntry);
		}

		return fileSizeMasterDataLogEntries;
	}

	private String findFileSize(String videoID, String resolution) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Top two most common resolutions in Vimeo (as of 24 Jan 2017): 360p (SD) and 720p (HD Ready)
	 * @param resolutionsList
	 * @return
	 */
	private boolean checkIfContainsTopTwoMostCommonResolutions(String[] resolutionsList) {

		ArrayList<String> resolutionsListAsArrayList = new ArrayList<String>(Arrays.asList(resolutionsList));
		if ((resolutionsListAsArrayList.contains("360p")) && (resolutionsListAsArrayList.contains("720p"))) {
			return true;
		} else {
			return false;
		}
	}

	private ArrayList<String> getVideoIDsToProcess(List<String> masterDataLogEntries, ArrayList<String> currentVideoIDsInFileSizeMaster) {

		ArrayList<String> videoIDsToProcess = new ArrayList<String>();

		for (String logEntry : masterDataLogEntries) {
			String[] logEntrySplitArray = logEntry.split("\t");
			if (logEntrySplitArray.length == 4) {
				//Log entry contains data about resolutions
				//Check if log entry has the 2 most common resolutions in Vimeo: 360p (SD) and 720p (HD Ready)
				//List of resolutions should be the fourth element in the string array
				String[] resolutionsList = (logEntrySplitArray[3]).split(", ");
				boolean containsTopTwoMostCommonResolutions = this.checkIfContainsTopTwoMostCommonResolutions(resolutionsList);
				if (containsTopTwoMostCommonResolutions) {	
					//check if video ID already exists in File Size Master Data File
					String videoID = logEntrySplitArray[0];
					if (currentVideoIDsInFileSizeMaster.contains(videoID)) {
						continue;
					} else {
						videoIDsToProcess.add(videoID);
					}
				}
			}
		}
		return videoIDsToProcess;
	}

	private ArrayList<String> getCurrentVideoIDsInFileSizeMaster() {

		ArrayList<String> currentVideoIDsInFileSizeMaster = new ArrayList<String>();

		try {
			//Read in data from file size master data file
			List<String> masterFileSizeDataLogEntries = Files.readAllLines(fileSizeMasterDataFilePath);
			for (String logEntry : masterFileSizeDataLogEntries) {
				String[] logEntrySplitArray = logEntry.split("\t");
				currentVideoIDsInFileSizeMaster.add(logEntrySplitArray[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		

		return currentVideoIDsInFileSizeMaster;
	}

}
