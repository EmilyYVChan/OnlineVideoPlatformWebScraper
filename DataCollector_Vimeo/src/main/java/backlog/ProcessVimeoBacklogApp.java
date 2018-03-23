package backlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import mainpackage.MainApplicationVimeo;

public class ProcessVimeoBacklogApp {
	
	public static void main(String[] args) {
		System.out.println("------------START------------");

		MainApplicationVimeo mainVimeoApp = new MainApplicationVimeo();
		try {
			//Console input: video ID from backlog file
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String videoID = br.readLine();
			
			//Store single video ID in an array list to use a method from MainApplicationVimeo.
			ArrayList<String> tempIDList = new ArrayList<String>();
			tempIDList.add(videoID);
			
			//Scrape Vimeo for video info
			List<String> logEntryList = mainVimeoApp.scrapeVimeoForVideoInfo(tempIDList);
			
			//Write to master data file
			System.out.println("STATUS: Recording log entry in master data file");
			Path masterDataFilePath = MainApplicationVimeo.masterDataFilePath;
			mainVimeoApp.recordInMasterFile(masterDataFilePath, logEntryList);
			
			System.out.println("----------COMPLETED----------");
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
