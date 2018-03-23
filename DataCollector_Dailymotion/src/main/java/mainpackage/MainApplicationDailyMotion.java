package mainpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainApplicationDailyMotion {

	public final static Path masterDataFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
			"SummerResearch_DataCollection" + File.separator + "Dailymotion" + File.separator + "Dailymotion_MasterData.txt");

	public final static Path backlogFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + 
			"SummerResearch_DataCollection" + File.separator + "Dailymotion" + File.separator + "Dailymotion_Backlog.txt");

	public static void main(String[] args) {

		System.out.println("------------START------------");

		MainApplicationDailyMotion app = new MainApplicationDailyMotion();

		//Read in video IDs of master data file and backlog data file and save in separate lists
		List<String> masterDataVideoIDListBeforeScrape = app.getListOfVideoIDsFromDataFile(masterDataFilePath);
		List<String> backlogVideoIDListBeforeScrape = app.getListOfVideoIDsFromDataFile(backlogFilePath);
		System.out.println("STATUS: Finished reading video IDs from Dailymotion master data file and backlog file");

		//Get today's date for the records
		String dateToday = app.getDateOfToday();
		
		//Copy current master data file and backlog data file into backup folder
		app.createBackupFiles("Dailymotion", dateToday);
		System.out.println("STATUS: Created backups of master data file and backlog data file");

		//Create a blank file to store today's data (e.g. Dailymotion_20170107.txt)
		Path dailymotionFilePath = app.createRawDataFile("Dailymotion", dateToday);
		System.out.println("STATUS: Finished creating blank file to store today's trending videos' IDs");

		//Scrape - today's trending videos in Dailymotion
		List<String> todayDataVideoIDList = app.scrapeDailymotion(dailymotionFilePath);
		System.out.println("STATUS: Finished scraping all of today's trending videos on Dailymotion");

		//Get a list of unique video IDs (those that have not been previously recorded)
		List<String> uniqueIDList = app.comparePreviousDataWithTodayData(masterDataVideoIDListBeforeScrape, backlogVideoIDListBeforeScrape, todayDataVideoIDList);
		System.out.println("STATUS: Finished acquiring a list of unique video IDs (those that have not been previously recorded)");

		//Crawl Dailymotion's API for more information for each unique video ID and store as a list of log entries
		List<String> logEntries = app.getVideoInfo(uniqueIDList);
		System.out.println("STATUS: Finished crawling Dailymotion for each unique video ID's relevant data.");

		//For each log entry, record it in the master data file
		app.recordInMasterFile(masterDataFilePath, logEntries);
		System.out.println("STATUS: Finished recording new (if any) log entries in master data file");

		//Show number of backlog entries before and after scrape
		List<String> backlogVideoIDListAfterScrape = app.getListOfVideoIDsFromDataFile(backlogFilePath);
		System.out.println("STATUS: Number of backlog entries before scrape: " + backlogVideoIDListBeforeScrape.size());
		System.out.println("STATUS: Number of backlog entries after scrape: " + backlogVideoIDListAfterScrape.size());

		//Show number of log entries in master data file before and after scrape
		List<String> masterDataVideoIDListAfterScrape = app.getListOfVideoIDsFromDataFile(masterDataFilePath);
		System.out.println("STATUS: Number of log entries in master file before scrape: " + masterDataVideoIDListBeforeScrape.size());
		System.out.println("STATUS: Number of log entries in master file after scrape: " + masterDataVideoIDListAfterScrape.size());

		//Show current number of log entries in collection
		int numLogEntries = masterDataVideoIDListAfterScrape.size() + backlogVideoIDListAfterScrape.size();
		System.out.println("STATUS: Total number of log entries, including backlog (Dailymotion): " + numLogEntries);

		System.out.println("----------COMPLETED----------");
	}

	private void recordInMasterFile(Path masterDataFilePath, List<String> logEntries) {

		for (String logEntry : logEntries) {
			try {
				Files.write(masterDataFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private List<String> getVideoInfo(List<String> uniqueIDList) {

		System.out.println("STATUS: Crawling Dailymotion for video info");

		List<String> logEntries = new ArrayList<String>();

		for (int i = 0; i < uniqueIDList.size(); i++) {

			String videoID = uniqueIDList.get(i);

			//Make a request call to the API for the required information, given a unique video ID
			String requestCallURL = "https://api.dailymotion.com/video/" + videoID + "?fields=title,channel,duration,available_formats";
			try {
				URL obj = new URL(requestCallURL);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				int responseCode = con.getResponseCode();

				if (responseCode == 200) { //successful request call
					//Read in JSON response
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer responseBuffer = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						responseBuffer.append(inputLine);
					}
					in.close();

					//Process received JSON response into a log entry with the desired format and store in list
					String responseJSON = responseBuffer.toString();
					JsonObject jsonObject = new JsonParser().parse(responseJSON).getAsJsonObject();

					String videoCategory = jsonObject.get("channel").getAsString(); //In Dailymotion, a category is termed as a 'channel'
					String videoDuration = jsonObject.get("duration").getAsString();

					//More processing required to get a video's resolutions. In dailymotion, it's termed as 'available formats'
					StringBuffer tempSB = new StringBuffer();
					JsonArray jsonArray = (JsonArray) jsonObject.get("available_formats");
					
					if (jsonArray.size() > 0) {
						for (JsonElement jsonElement : jsonArray) {
							String format = jsonElement.getAsString();
							tempSB.append(format + ", ");
						}
					}
					//remove the last ", " if appropriate. StringIndexOutOfBoundsException will be thrown if there is no ", " to remove
					String videoResolutions = "";
					
					try {
						videoResolutions = tempSB.substring(0, tempSB.length()-2);
						
					} catch (java.lang.StringIndexOutOfBoundsException e) {
						videoResolutions = tempSB.toString();
					}
					
					String logEntry = videoID + "\t" + videoCategory + "\t" + videoDuration + "\t" + videoResolutions + "\t\r\n";

					logEntries.add(logEntry);

					System.out.print("Log entry #" + (i+1) + "/" + uniqueIDList.size() + ": " + logEntry);
					

				} else { //not a successful request call. Save Video ID in backlog file.
					System.out.println("ERROR with log entry #" + (i+1) + ": " + videoID);
					String backlogEntry = videoID + "\r\n";

					try {
						Files.write(backlogFilePath, backlogEntry.getBytes(), StandardOpenOption.APPEND);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} 

		}
		return logEntries;
	}

	private List<String> comparePreviousDataWithTodayData(List<String> masterDataVideoIDList, List<String> backlogVideoIDList, List<String> todayDataVideoIDList) {

		List<String> uniqueIDList = new ArrayList<String>();

		for (String id : todayDataVideoIDList) {
			if ((!(masterDataVideoIDList.contains(id))) && (!(backlogVideoIDList.contains(id)))) { //ID is unique
				uniqueIDList.add(id);
			} 
		}

		return uniqueIDList;
	}

	private List<String> getListOfVideoIDsFromDataFile(Path dataFilePath) {

		List<String> videoIDList = new ArrayList<String>();

		try {
			List<String> dataRecordLines = Files.readAllLines(dataFilePath, StandardCharsets.ISO_8859_1);
			for (String line: dataRecordLines) {
				String[] temp = line.split("\t");
				videoIDList.add(temp[0]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return videoIDList;
	}

	/**
	 * Scrape today's internationally trending videos in Dailymotion.
	 * These videos are shown in 'http://www.dailymotion.com/en/trending/'
	 * There are 1800 videos - 18 videos per page, with 100 pages.
	 * Save video IDs of trending videos in the file specified by given file path.
	 * 
	 * @param dailymotionRawDataFilePath
	 */
	private List<String> scrapeDailymotion(Path dailymotionRawDataFilePath) {

		List<String> todayData = new ArrayList<String>();

		for (int i = 0; i < 100; i++) { //Scrape all 100 pages

			System.out.println("STATUS: Scraping today's trending videos on page " + (i+1));

			String url = "http://www.dailymotion.com/en/trending/" + (i+1); //URL of webpage to scrape

			try {
				Document doc = Jsoup.connect(url) //Connect to url
						.userAgent("Mozilla/5.0") //Add a request header -> set user agent of connection
						.get(); 				  //Set request method as GET and execute.

				//Extract only the webpage section containing info of the videos (a specific div class)
				//Only collecting non-live videos.
				Elements elemsVideosNonLive = doc.getElementsByClass("sd_video_preview media-img mrg-end-lg span-3");	 //for non-live videos

				//Non live videos
				for (int j = 0; j < elemsVideosNonLive.size(); j++) {
					String videoID = elemsVideosNonLive.get(j).attr("data-id"); //Get the video ID

					todayData.add(videoID);

					//Log video ID in given file path
					String logEntry = videoID + "\r\n"; 
					Files.write(dailymotionRawDataFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		return todayData;

	}

	private Path createRawDataFile(String keyword, String dateToday) {

		//Setup file name
		String filePathName = "C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + 
				"SummerResearch_DataCollection" + File.separator + keyword + File.separator + "Raw" + File.separator + keyword + "_" + dateToday + ".txt";

		//Setup file path
		Path filePath = Paths.get(filePathName);
		try {
			Files.createFile(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filePath;
	}

	private void createBackupFiles(String keyword, String dateToday) {
		//Master data backup file path
		Path masterDataBackupFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + 
				"SummerResearch_DataCollection" + File.separator + keyword + File.separator + "Backup" + File.separator + keyword + "_MasterDataBackup_Prescrape_" + dateToday + ".txt");
		
		//Backlog data backup file path
		Path backlogBackupFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + 
				"SummerResearch_DataCollection" + File.separator + keyword + File.separator + "Backup" + File.separator + keyword + "_BacklogBackup_Prescrape_" + dateToday + ".txt");
		
		try {
			Files.copy(masterDataFilePath, masterDataBackupFilePath, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(backlogFilePath, backlogBackupFilePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private String getDateOfToday() {
		LocalDate localDate = LocalDate.now();
		String dateToday = DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate);
		return dateToday;
	}

}
