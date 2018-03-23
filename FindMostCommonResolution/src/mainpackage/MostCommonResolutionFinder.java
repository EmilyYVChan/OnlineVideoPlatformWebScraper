package mainpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MostCommonResolutionFinder {

	public static void main(String[] args) throws IOException {
		//File path of data file to analyse. TODO Adjust this file path as needed
		//Data file must only contain lines of the following format (without the square brackets):
		//[videoID]\t[videoCategory]\t[videoDuration]\t[videoResolutions]
//		Path filePath = Paths.get(
//				"C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
//				"SummerResearch_DataCollection" + File.separator + "Vimeo" + File.separator + "Vimeo_MasterData.txt");

				Path filePath = Paths.get(
						"C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
						"SummerResearch_DataCollection" + File.separator + "Dailymotion" + File.separator + "Dailymotion_MasterData.txt");

		//		Path filePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator
		//				+ "test.txt");


		//(Map)List of resolutions and its corresponding number of times appeared in file
		HashMap<String,Integer> resolutionsList = new HashMap<String,Integer>(); 

		//Read all lines in data file - store in list
		List<String> dataLogs = Files.readAllLines(filePath);

		//For each line, split by regex = \t
		for (String log : dataLogs) {
			String[] splitLog = log.split("\t");
			//Resolutions should be in the 4th position in the splitLog array.
			String[] resolutionsOfLog = new String[10];
			try {
				resolutionsOfLog = (splitLog[3]).split(",");
			} catch (ArrayIndexOutOfBoundsException e) {
				//If not, disregard and continue
				continue;
			}
			
			//For each resolution in each line
			for (int i = 0; i < resolutionsOfLog.length; i++) {
				String resolution = resolutionsOfLog[i];

				//If resolution = "Auto", disregard and continue
				if (resolution.equals("Auto")) {
					continue;
				}

				//Check if resolution type already exists in list of resolution
				//If yes, increment count of that resolution type

				if (resolutionsList.containsKey(resolution)) {
					Integer count = resolutionsList.get(resolution);
					resolutionsList.put(resolution, (count+1));
				}
				//If no, add to list of resolutions and increment by one
				else {
					resolutionsList.put(resolution, 1);
				}
			}

		}

		//For each resolution in List of resolutions, print out resolution and count value
		Integer largestCount = 0;
		Integer secondLargestCount = 0;
		for (String resolution : resolutionsList.keySet()) {
			Integer countValue = resolutionsList.get(resolution);
			System.out.println(resolution + "\t: " + countValue);

			if (countValue.compareTo(largestCount) > 0) {
				largestCount = countValue;
			}
		}
		
		for (String resolution : resolutionsList.keySet()) {
			Integer countValue = resolutionsList.get(resolution);
			if ((countValue.compareTo(largestCount) < 0) && (countValue.compareTo(secondLargestCount) > 0)) {
				secondLargestCount = countValue;
			}
		}

		ArrayList<String> resolutionsOfLargestCount = new ArrayList<String>();
		ArrayList<String> resolutionsOfSecondLargestCount = new ArrayList<String>();

		for (String resolution : resolutionsList.keySet()) {
			if (resolutionsList.get(resolution).equals(largestCount)) {
				resolutionsOfLargestCount.add(resolution);
			} else if (resolutionsList.get(resolution).equals(secondLargestCount)) {
				resolutionsOfSecondLargestCount.add(resolution);
			}
		}

		System.out.println("\nFile: \t" + filePath.getFileName());
		System.out.println("1st most common resolution(s):\t" + largestCount + "\t" + resolutionsOfLargestCount.toString());
		System.out.println("2nd most common resolution(s):\t" + secondLargestCount + "\t" + resolutionsOfSecondLargestCount.toString());
	}

}
