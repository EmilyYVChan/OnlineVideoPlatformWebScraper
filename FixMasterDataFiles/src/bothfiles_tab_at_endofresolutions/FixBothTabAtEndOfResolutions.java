package bothfiles_tab_at_endofresolutions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FixBothTabAtEndOfResolutions {

	public static void main(String[] args) throws IOException {

		//New master data file 
		Path newFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator + "test.txt");

		//Read in data from file
		Path oldFilePath = Paths.get("C:" + File.separator + "Users" + File.separator + "echa232" + File.separator + "Documents" + File.separator +
				"SummerResearch_DataCollection" + File.separator + "Vimeo" + File.separator + "Vimeo_MasterData.txt");
		List<String> oldLogEntries = Files.readAllLines(oldFilePath);

		//for each line
		for (String oldLogEntry : oldLogEntries) {
			//replace the ending sequence of characters "\r\n" with "\t\r\n"
			String newLogEntry = oldLogEntry + "\t\r\n";
			Files.write(newFilePath, newLogEntry.getBytes(), StandardOpenOption.APPEND);

		}

	}

}
