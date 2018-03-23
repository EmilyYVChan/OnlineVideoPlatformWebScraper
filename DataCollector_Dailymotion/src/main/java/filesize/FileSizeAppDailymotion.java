package filesize;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import mainpackage.MainApplicationDailyMotion;

public class FileSizeAppDailymotion {

	public static final String desiredResolution1 = "ld";
	public static final String desiredResolution2 = "sd";

	//TODO COOKIE value changes each day. Needs updating for this program to work!
	public static final String COOKIE = "PHPSESSID=l583cobumv6m81975bemiofmf0; _gat=1; _ga=GA1.2.852271422.1486699515";
	public static final String PARENTFOLDER = "C:\\Users\\echa232\\Documents\\SummerResearch_DataCollection\\Dailymotion\\FileSize\\Batch_10_9001-10000\\";
	public static final Path fileSizeDataFilePath = Paths.get(PARENTFOLDER + "Dailymotion_MasterFileSizeData.txt");
	public static final Path fileSizeBacklogFilePath  = Paths.get(PARENTFOLDER + "Dailymotion_BacklogFileSizeData.txt");
	public static final Path masterDataFilePath = Paths.get(PARENTFOLDER + "Dailymotion_MasterData.txt");
	

	public static void main(String[] args) {

		System.out.println("------------START------------");
 
		FileSizeAppDailymotion app = new FileSizeAppDailymotion();

		//Read in record logs from Dailymotion Master Data file
		List<List<String>> listOfMasterDataLogs = app.readMasterDataLogs();
		System.out.println("STATUS: Finished reading in data from Dailymotion master data file");

		//Get only those record logs that have both LD and SD resolutions
		List<List<String>> listOfLogsWithBothDesiredResolutions = app.extractLogsWithBothDesiredResolutions(listOfMasterDataLogs);
		System.out.println("STATUS: Finished sorting to get only record logs that have both LD and SD resolutions");

		//Get file size for desired resolution 1
		List<String> listOfFileSizesOfResolution1 = app.addFileSizeToEachLog(listOfLogsWithBothDesiredResolutions, desiredResolution1);
		System.out.println("STATUS: Finished acquiring file sizes of each video in LD resolution");

		//Get file sizes for desired resolution 2
		List<String> listOfFileSizesOfResolution2 = app.addFileSizeToEachLog(listOfLogsWithBothDesiredResolutions, desiredResolution2);
		System.out.println("STATUS: Finished acquiring file sizes of each video in SD resolution");

		//Calculate data rate for desired resolution 1
		List<String> listOfDataRatesOfResolution1 = app.addDataRatesToEachLog(listOfLogsWithBothDesiredResolutions, listOfFileSizesOfResolution1);
		System.out.println("STATUS: Finished calculating data rates of each video in LD resolution");

		//Calculate data rate for desired resolution 2
		List<String> listOfDataRatesOfResolution2 = app.addDataRatesToEachLog(listOfLogsWithBothDesiredResolutions, listOfFileSizesOfResolution2);
		System.out.println("STATUS: Finished calculating data rates of each video in SD resolution");

		//Write data to file
		app.writeDataToFile(listOfLogsWithBothDesiredResolutions, listOfFileSizesOfResolution1, listOfFileSizesOfResolution2, listOfDataRatesOfResolution1, listOfDataRatesOfResolution2);
		System.out.println("STATUS: Finished writing data to new file");

		System.out.println("----------COMPLETED----------");
	}

	private void writeDataToFile(List<List<String>> listOfLogsWithBothDesiredResolutions,
			List<String> listOfFileSizesOfResolution1, List<String> listOfFileSizesOfResolution2,
			List<String> listOfDataRatesOfResolution1, List<String> listOfDataRatesOfResolution2) {

		//First check if lengths of all lists match. If unmatched, means something went wrong earlier. 
		if (listOfLogsWithBothDesiredResolutions.size() != listOfFileSizesOfResolution1.size()) {
			throw new IllegalArgumentException("Number of logs do not match number of calculated file sizes for resolution 1!");
		} else if (listOfLogsWithBothDesiredResolutions.size() != listOfFileSizesOfResolution2.size()) {
			throw new IllegalArgumentException("Number of logs do not match number of calculated file sizes for resolution 2!");
		} else if (listOfLogsWithBothDesiredResolutions.size() != listOfDataRatesOfResolution1.size()) {
			throw new IllegalArgumentException("Number of logs do not match number of calculated data rates for resolution 1!");
		} else if (listOfLogsWithBothDesiredResolutions.size() != listOfDataRatesOfResolution2.size()) {
			throw new IllegalArgumentException("Number of logs do not match number of calculated data rates for resolution 2!");
		} else {
			
			for (int i = 0; i < listOfLogsWithBothDesiredResolutions.size(); i++) {
				try {
					StringBuffer tempSB = new StringBuffer();
					List<String> originalLog = listOfLogsWithBothDesiredResolutions.get(i);
					String fileSizeOfResolution1 = listOfFileSizesOfResolution1.get(i);
					String fileSizeOfResolution2 = listOfFileSizesOfResolution2.get(i);
					String dataRateOfResolution1 = listOfDataRatesOfResolution1.get(i);
					String dataRateOfResolution2 = listOfDataRatesOfResolution2.get(i);

					if ((fileSizeOfResolution1.equals("0")) || (fileSizeOfResolution2.equals("0"))) {
						continue;
						//Error occurred earlier. Not recording it in file size data master file
					}

					for (int j = 0; j < originalLog.size(); j++) {
						tempSB.append(originalLog.get(j) + "\t");
					}

					tempSB.append(fileSizeOfResolution1 + "\t");
					tempSB.append(fileSizeOfResolution2 + "\t");
					tempSB.append(dataRateOfResolution1 + "\t");
					tempSB.append(dataRateOfResolution2 + "\t");

					String logEntry = tempSB.toString() + "\r\n";
					Files.write(fileSizeDataFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private List<String> addDataRatesToEachLog(List<List<String>> listOfLogsWithBothDesiredResolutions,	List<String> listOfFileSizesOfResolution) {
		List<String> dataRatesOfResolution = new ArrayList<String>();

		//First check if lengths of both lists match. If unmatched, means something went wrong earlier.
		if (listOfLogsWithBothDesiredResolutions.size() != listOfFileSizesOfResolution.size()) {
			throw new IllegalArgumentException("Number of calculated file size values do not match number of logs!");
		}

		//For each log in list
		//Get duration
		//Get file size of desired resolution 1
		//Get bit rate: Divide file size by duration
		for (int i = 0; i < listOfLogsWithBothDesiredResolutions.size(); i++) {
			List<String> log = listOfLogsWithBothDesiredResolutions.get(i);
			double duration = Double.parseDouble(log.get(2));
			double fileSize = Double.parseDouble(listOfFileSizesOfResolution.get(i));

			double bitRate = fileSize/duration;

			dataRatesOfResolution.add(Double.toString(bitRate));
		}

		return dataRatesOfResolution;
	}

	private void sendPostTransload(String videoID, String videoURL, String ext, String conv, String format, String sourceURL, String audioURL) throws IOException {

		String url = "http://www.telechargerunevideo.com/en/transload";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		con.setRequestProperty("Connection", "keep-alive");
		con.setRequestProperty("Cookie", COOKIE);
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Host", "www.telechargerunevideo.com");
		con.setRequestProperty("Referer", "http://www.telechargerunevideo.com/en/");
		con.setRequestProperty("User-Agent", "Mozilla/5.0"); 

		String urlParameters = "videoid=" + videoID + "&videourl=" + videoURL + "&ext=" + ext + "&conv=" + conv + "&format=" + format + "&sourceurl=" + sourceURL + "&audiourl=" + audioURL;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		if (responseCode == 200) {
			return;
		} else {
			System.out.println("Error in send post transload");
		}

	}

	private void convertProgress(String videoID) throws IOException {
		String url = "http://www.telechargerunevideo.com/en/conv_progress1?videoid=" + videoID;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//Add the necessary request headers
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		con.setRequestProperty("Connection", "keep-alive");
		con.setRequestProperty("Cookie", COOKIE);
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Host", "www.telechargerunevideo.com");
		con.setRequestProperty("Referer", "http://www.telechargerunevideo.com/en/");
		con.setRequestProperty("User-Agent", "Mozilla/5.0"); 

		int responseCode = con.getResponseCode();

		if (responseCode == 200) {
			return;
		} else {
			System.out.println("error during convert progress");
		}

	}

	private void sendPostConvert(String videoID) throws IOException {
		String url = "http://www.telechargerunevideo.com/en/convert";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		con.setRequestProperty("Connection", "keep-alive");
		con.setRequestProperty("Cookie", COOKIE);
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Host", "www.telechargerunevideo.com");
		con.setRequestProperty("Referer", "http://www.telechargerunevideo.com/en/");
		con.setRequestProperty("User-Agent", "Mozilla/5.0"); 

		String urlParameters = "videoid=" + videoID;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

	}

	private void transProgress(String videoID) throws IOException {

		String url = "http://www.telechargerunevideo.com/en/trans_progress1?videoid=" + videoID;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//Add the necessary request headers
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		con.setRequestProperty("Connection", "keep-alive");
		con.setRequestProperty("Cookie", COOKIE);
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Host", "www.telechargerunevideo.com");
		con.setRequestProperty("Referer", "http://www.telechargerunevideo.com/en/");
		con.setRequestProperty("User-Agent", "Mozilla/5.0"); 

		int responseCode = con.getResponseCode();

		if (responseCode == 200) {
			return;
		} else {
			System.out.println("error during transProgress");
		}

	}

	private String getFileSize(String dailymotionVideoID, String desiredResolution) {

		String fileSize = "";

		String urlString = "http://www.telechargerunevideo.com/en/getvideo?url=http%3A%2F%2Fwww.dailymotion.com%2Fvideo%2F" + dailymotionVideoID;

		try {
			//Setup and open connection
			URL url = new URL(urlString);		
			HttpURLConnection con;
			con = (HttpURLConnection) url.openConnection();

			//Add the necessary request headers
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", COOKIE);
			con.setRequestProperty("DNT", "1");
			con.setRequestProperty("Host", "www.telechargerunevideo.com");
			con.setRequestProperty("Referer", "http://www.telechargerunevideo.com/en/");
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

				//Process received JSON response: get the necessary form data from JSON response to make HTTP POST request later
				String responseJSON = responseBuffer.toString();
				try {

					JsonObject jsonObjectInResponse = new JsonParser().parse(responseJSON).getAsJsonObject();

					JsonArray jsonArray = (JsonArray) jsonObjectInResponse.get("links");

					//Necessary form data
					String downloadVideoID = jsonObjectInResponse.get("videoid").getAsString();
					String videoURL = "";
					String ext = "";
					String conv = "";
					String format = "";
					String sourceURL = jsonObjectInResponse.get("sourceurl").getAsString();
					String audioURL = jsonObjectInResponse.get("audio").getAsString();

					String expectedQuality = "";
					if (desiredResolution.equals(desiredResolution1)) {
						expectedQuality = "Low - 240p";
					} else if (desiredResolution.equals(desiredResolution2)) {
						expectedQuality = "Medium - 384p";
					}

					for (JsonElement jsonElement : jsonArray) {
						JsonObject jsonObjectInArray = jsonElement.getAsJsonObject();
						String actualQuality = jsonObjectInArray.get("quality").getAsString();
						if (actualQuality.equals(expectedQuality)) {
							videoURL = jsonObjectInArray.get("url").getAsString();
							ext = jsonObjectInArray.get("ext").getAsString();
							conv = jsonObjectInArray.get("conv").getAsString();
							format = jsonObjectInArray.get("frmt").getAsString();
						} 
					}

					//4 XHR calls to be executed before final download URL link is available
					this.sendPostTransload(downloadVideoID, videoURL, ext, conv, format, sourceURL, audioURL);
					this.transProgress(downloadVideoID);
					this.sendPostConvert(downloadVideoID);
					this.convertProgress(downloadVideoID);

					//Setup and open new connection for the download URL link
					String newURLString = "http://www.telechargerunevideo.com/en/download?id=" + downloadVideoID;
					URL newURL = new URL(newURLString);		
					HttpURLConnection newCon = (HttpURLConnection) newURL.openConnection();

					newCon.setRequestMethod("GET");
					newCon.setRequestProperty("User-Agent", "Mozilla/5.0"); 
					newCon.setRequestProperty("Range", "bytes=" + 0 + "-" + 1);
					//Response code will be 206 (Partial Content) instead of usual 200 (OK) because only file size information is needed
					if(newCon.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
						String contentRange = newCon.getHeaderField("Content-Range");
						fileSize = contentRange.replaceAll("bytes 0-1/", "");
					}
				} catch (java.lang.IllegalStateException e) {
					//					System.out.println(responseJSON);
					//					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (fileSize.equals("")) {
			System.out.println("ERROR: Couldn't find file size for ID: " + dailymotionVideoID + "\t Resolution: " + desiredResolution);

			//Error occurred. Save dailymotion video ID and desired resolution in a record entry to backlog file.
			try {
				String backlogEntry = dailymotionVideoID + "\t" + desiredResolution + "\r\n";

				Files.write(fileSizeBacklogFilePath, backlogEntry.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}

			fileSize = "0";
		}

		return fileSize;
	}

	private List<String> addFileSizeToEachLog(List<List<String>> listOfLogsWithBothDesiredResolutions, String desiredResolution) {
		//For each log in list
		//Get video ID
		//Get file size, depending on desiredResolution
		//Append file size value to end of log

		List<String> listOfCorrespondingFileSizes = new ArrayList<String>();

		for (int i = 0; i < listOfLogsWithBothDesiredResolutions.size(); i++) {
			List<String> log = listOfLogsWithBothDesiredResolutions.get(i);
			String dailymotionVideoID = log.get(0);
			String fileSize = this.getFileSize(dailymotionVideoID, desiredResolution);
			listOfCorrespondingFileSizes.add(fileSize);	
			System.out.println("STATUS: Progress = " + (i+1) + "/" + listOfLogsWithBothDesiredResolutions.size() + " for " + desiredResolution + " resolution.");
		}

		return listOfCorrespondingFileSizes;
	}

	private List<List<String>> extractLogsWithBothDesiredResolutions(List<List<String>> listOfMasterDataLogs) {

		List<List<String>> listOfLogsWithBothDesiredResolutions = new ArrayList<List<String>>(); 

		//For each log in list of master data logs,
		//check if the log's list of resolutions (should be in 4th cell in log array) contains both desired resolutions
		//If yes, then store log.

		for (List<String> log : listOfMasterDataLogs) {
			try {
				List<String> listOfResolutions = Arrays.asList(log.get(3).split(", "));
				if ((listOfResolutions.contains(desiredResolution1)) && (listOfResolutions.contains(desiredResolution2))) {
					listOfLogsWithBothDesiredResolutions.add(log);
				} 
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				//If caught this exception, then it means that there are no resolutions for this video at all
				continue;
			}
			
		}
		return listOfLogsWithBothDesiredResolutions;
	}

	private List<List<String>> readMasterDataLogs() {

		List<List<String>> listOfMasterDataLogs = new ArrayList<List<String>>(); 

		try {
//			Path testPath = Paths.get("C:\\Users\\echa232\\Documents\\testMasterDailymotion.txt");
//			List<String> dataRecordLines = Files.readAllLines(testPath, StandardCharsets.ISO_8859_1);
			
			List<String> dataRecordLines = Files.readAllLines(masterDataFilePath, StandardCharsets.ISO_8859_1);


			for (String line: dataRecordLines) {
				String[] temp = line.split("\t");
				listOfMasterDataLogs.add(Arrays.asList(temp));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listOfMasterDataLogs;
	}

}
