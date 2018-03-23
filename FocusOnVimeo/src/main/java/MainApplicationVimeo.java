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
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainApplicationVimeo {
	
	public final static String masterDataFilePathName = "C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
			"SummerResearch_DataCollection" + File.separator + "Vimeo" + File.separator + "Vimeo_MasterData.txt";

	public static void main(String[] args) {
		
		System.out.println("------------START------------");
		
		MainApplicationVimeo app = new MainApplicationVimeo();

		//Read in video IDs of master data file and save in a list
		Path masterDataFilePath = Paths.get(masterDataFilePathName);
		List<String> masterDataVideoIDList = app.getListOfVideoIDsFromMasterDataFile(masterDataFilePath);
		System.out.println("STATUS: Finished reading video IDs from Vimeo master data file");

		//Get today's date for the records
		String dateToday = app.getDateOfToday();

		//Create blank files to store today's data (e.g. Dailymotion_20170107.txt or Vimeo_20170107.txt)
		Path vimeoFilePath = app.createFilePath("Vimeo", dateToday);
		System.out.println("STATUS: Finished creating blank file to store today's trending videos' IDs");

		//Scrape - today's trending videos in Vimeo
		List<String> todayDataVideoIDList = app.scrapeVimeoForTrendingVideosID(vimeoFilePath);
		System.out.println("STATUS: Finished scraping all of today's trending videos on Vimeo");

		//Get a list of unique video IDs (those that have not been previously recorded)
		List<String> uniqueIDList = app.compareMasterDataWithTodayData(masterDataVideoIDList, todayDataVideoIDList);
		System.out.println("STATUS: Finished acquiring a list of unique video IDs (those that have not been previously recorded)");

		//Scrape Vimeo for more information for each unique video ID and store as a list of log entries
		List<String> logEntries = app.scrapeVimeoForVideoInfo(uniqueIDList);
		System.out.println("STATUS: Finished scraping Vimeo for each unique video ID's relevant data.");

		//For each log entry, record it in the master data file
		app.recordInMasterFile(masterDataFilePath, logEntries);
		System.out.println("STATUS: Finished recording new (if any) log entries in master data file");

		//Show current number of unique log entries
		System.out.println("STATUS: Number of new log entries today (Vimeo): " + uniqueIDList.size());
		
		//Show current number of log entries in master data file
		int numLogEntries = masterDataVideoIDList.size() + uniqueIDList.size();
		System.out.println("STATUS: Total number of log entries (Vimeo): " + numLogEntries);
		
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

	private List<String> scrapeVimeoForVideoInfo(List<String> uniqueIDList) {
		System.out.println("STATUS: Scraping Vimeo for video info");

		List<String> logEntries = new ArrayList<String>();

		// Setting the location of the chrome driver in the system properties
		System.setProperty(
				"webdriver.chrome.driver",
				"C:\\Program Files\\Java\\chromedriver.exe");
		WebDriver webDriver = new ChromeDriver();

		// Setting the browser size
		webDriver.manage().window().setSize(new Dimension(1360, 768));

		for (String id : uniqueIDList) {

			// Go to Vimeo video URL
			webDriver.navigate().to("https://vimeo.com/" + id);

			// Get video duration
			WebElement videoDurationElement = webDriver.findElement(By.xpath("//*[@id=\"" + id + "\"]/div[5]/div[2]/div/div[1]/div[2]"));
			String videoDurationValue = videoDurationElement.getAttribute("aria-valuemax");

			// Get video resolutions
			String videoResolutions = this.getVideoInfoResolutions(webDriver);

			// Get video category	
			String videoCategories = this.getVideoInfoCategories(webDriver);

			String logEntry = id + "\t" + videoCategories + "\t" + videoDurationValue + "\t" + videoResolutions + "\r\n";
			logEntries.add(logEntry);

			System.out.println("Log entry: " + logEntry);
		}
		// Closing the browser and webdriver
		webDriver.close();
		webDriver.quit();
		
		return logEntries;
	}
	
	private String getVideoInfoResolutions(WebDriver webDriver) {
		
		String videoResolutions = "";
		
		//First, check if there are multiple resolutions available
		List<WebElement> tempList = webDriver.findElements(By.className("hd"));
		if (tempList.size() == 0) {
			return videoResolutions;
		} else {
			WebElement tempVideoResButtonElement = tempList.get(0);
			tempVideoResButtonElement.click();
			String tempVideoResMenuID = tempVideoResButtonElement.getAttribute("aria-controls");
			WebElement tempVideoResMenuElement = webDriver.findElement(By.id(tempVideoResMenuID));
			videoResolutions = tempVideoResMenuElement.getText().replaceAll("\n", ", ");
			return videoResolutions;
		}		
	}

	private String getVideoInfoCategories(WebDriver webDriver) {
		StringBuffer vidCategoriesStringBuffer = new StringBuffer();
		String videoCategories = "";
		
		//First, check if there is any extra info (creditors and categories) associated with the video
		List<WebElement> extraInfoElementsList = webDriver.findElements(By.className("clip-extras"));
		if (extraInfoElementsList.size() == 0) { 
			return videoCategories;
		}
		
		//Second, check if there is a 'View all' URL for categories (disregard the one (if there is one) for creditors)
			//If there is, follow the URL and get all categories
		WebElement extraInfoElement = extraInfoElementsList.get(0);
		List<WebElement> viewAllElements = extraInfoElement.findElements(By.linkText("View all"));
		if (viewAllElements.size() != 0) {
			for (WebElement tempElem : viewAllElements) {
				String linkToViewAll = tempElem.getAttribute("href"); //get the URL link to view all (can lead to list of creditors or categories)
				if (linkToViewAll.contains("collections")) { //means the link refers to categories, not credits
					//Visit link and collect all of video's categories' names
					webDriver.navigate().to(linkToViewAll);
					List<WebElement> listOfCategoryTitleElements = webDriver.findElements(By.className("title"));
					for (WebElement categoryTitleElement : listOfCategoryTitleElements) {
						vidCategoriesStringBuffer.append(categoryTitleElement.getText() + ", ");
					}
					videoCategories = vidCategoriesStringBuffer.substring(0, vidCategoriesStringBuffer.length()-2); //Removes the last ', '
					return videoCategories;
				}
			}
		} 
		
		//Third, check if there is any categories associated with the video.
			//If there is (should be less than or equal to 3 categories), scrape them from the video's webpage itself.
		List<WebElement> categoriesElementsList = webDriver.findElements(By.className("clip-categories"));
		if (categoriesElementsList.size() == 0) {
			return videoCategories;
		} else {
			String categoriesElementText = categoriesElementsList.get(0).getText();
			vidCategoriesStringBuffer.append(categoriesElementText.replaceAll("\n", ", "));
			videoCategories = vidCategoriesStringBuffer.toString();
			return videoCategories;
		}
	}
	
	private List<String> compareMasterDataWithTodayData(List<String> masterDataVideoIDList, List<String> todayDataVideoIDList) {
		List<String> uniqueIDList = new ArrayList<String>();

		for (String id : todayDataVideoIDList) {
			if (!(masterDataVideoIDList.contains(id))) { //ID is unique
				uniqueIDList.add(id);
			}
		}
		return uniqueIDList;
	}

	/** 
	 * Scrape today's internationally trending videos in Vimeo.
	 * Vimeo does not distinguish trending videos based on geolocation.
	 * There are about 1000 trending videos shown in Vimeo's main page: 'https://vimeo.com/'
	 * These videos are shown in a thumbnail grid under the 'See what's trending' section in a horizontal scroll.
	 * Save video IDs of today's trending videos in the file specified by given file path.
	 * 
	 * @param vimeoFilePath
	 */
	private List<String> scrapeVimeoForTrendingVideosID(Path vimeoRawDataFilePath) {
		
		List<String> todayDataVideoIDList = new ArrayList<String>();

		for (int pageOffsetValue = 0; pageOffsetValue <= 1042; pageOffsetValue += 48) {
			System.out.println("STATUS: Scraping today's trending videos on page offset = " + pageOffsetValue);			

			String url = "https://vimeo.com/explore_data?action=videos&page_offset=" + pageOffsetValue + "&context_type=popular";
			try {
				URL obj;

				obj = new URL(url);

				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				//Add the necessary request headers
				con.setRequestMethod("GET");
				con.setRequestProperty("Accept", "application/json");
				con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
				con.setRequestProperty("Connection", "keep-alive");
				con.setRequestProperty("Cookie", "__ssid=0494aa8b-5a33-49d7-95f5-87573451fd4a; continuous_play_v3=1; player=\"\"; _abexps=%7B%22158%22%3A%22A%22%2C%22200%22%3A%22on%22%7D; _ga=GA1.2.903324275.1483577267; _gat_UA-76641-8=1; vuid=915217781.1823514841; _abexps=%7B%22158%22%3A%22A%22%2C%22200%22%3A%22on%22%7D");
				con.setRequestProperty("DNT", "1");
				con.setRequestProperty("Host", "vimeo.com");
				con.setRequestProperty("Referer", "https://vimeo.com/");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");        
				con.setRequestProperty("x-requested-with", "XMLHttpRequest");

				BufferedReader br = null;
				if ("gzip".equals(con.getContentEncoding())) {
					br = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
				}
				else {
					br  = new BufferedReader(new InputStreamReader(con.getInputStream()));
				}				

				//Process JSON response to get video IDs only
				JsonElement jsonElement = new JsonParser().parse(br); //is a JsonObject containing only one element: videos (which is a json Array)
				//get as object
				JsonObject jsonObject = jsonElement.getAsJsonObject(); //videos
				//get as array
				JsonArray jsonArray = jsonObject.getAsJsonArray("videos");
				//loop over array and get clip id only
				for (JsonElement element : jsonArray) {
					JsonObject temp = element.getAsJsonObject();
					String videoID = temp.get("clip_id").getAsString(); //Video IDs are termed as 'clip IDs' in Vimeo
					todayDataVideoIDList.add(videoID);
					
					//Log video ID in given file path
					String logEntry = videoID + "\r\n"; 
					Files.write(vimeoRawDataFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
				}				

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return todayDataVideoIDList;
	}

	private Path createFilePath(String keyword, String dateToday) {

		//Setup file name
		String filePathName = "C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + 
				"SummerResearch_DataCollection" + File.separator + keyword + File.separator + "Raw" + File.separator + keyword + "_" + dateToday + ".txt";

		//Setup file path (and relevant directories if they don't already exist)
		Path filePath = Paths.get(filePathName);
		try {
			Files.createFile(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filePath;
	}

	private String getDateOfToday() {
		LocalDate localDate = LocalDate.now();
		String dateToday = DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate);
		return dateToday;
	}

	private List<String> getListOfVideoIDsFromMasterDataFile(Path masterDataFilePath) {

		List<String> masterDataVideoIDList = new ArrayList<String>();

		try {
			List<String> dataRecordLines = Files.readAllLines(masterDataFilePath, StandardCharsets.ISO_8859_1);
			for (String line: dataRecordLines) {
				String[] temp = line.split("\t");
				masterDataVideoIDList.add(temp[0]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return masterDataVideoIDList;
	}

}
