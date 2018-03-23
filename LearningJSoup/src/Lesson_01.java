import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Lesson_01 {

	public static void main(String[] args) throws IOException {
		//		//Parse string HTML
		//		String html = "<html><head><title>First parse</title></head>"
		//				+ "<body><p>Parsed HTML into a doc.</p></body></html>";
		//		Document doc = Jsoup.parse(html);
		//		System.out.println(doc);
		//		
		//		//Parse URL HTML - 1st try
		//		Connection con = Jsoup.connect("http://www.dailymotion.com/en/trending/100");
		//		con = con.userAgent("Mozilla/5.0");
		////		con = con.method(Connection.Method.GET);
		////		Connection.Response resp = con.execute();
		//		Document doc = con.get();
		//		System.out.println(doc);
		//		
//				//Parse URL HTML - shorter version. achieves same thing as 1st try.
//		String url = "https://api.dailymotion.com/video/x57m2z1?fields=id,title";
//				Document doc = Jsoup.connect(url)
//						.userAgent("Mozilla/5.0")
//						.get();
//				System.out.println(doc);
		//		
		//		//Parse URL HTML - get body of html document only
		//		String url = "http://www.dailymotion.com/en/trending/1"; //url of webpage to scrape
		//		
		//		Document doc = Jsoup.connect(url) //connect to url
		//				.userAgent("Mozilla/5.0") //add a request header -> set user agent of connection
		//				.get();					  //set request method as GET and execute. 
		//		Element elem = doc.body();		  //Extract the body of html webpage only.
		//		System.out.println(elem);
		//		
		//		//Parse URL HTML - get the div with the 18 videos on DM trending page only
		//		String url = "http://www.dailymotion.com/en/trending/1"; //url of webpage to scrape
		//		
		//		Document doc = Jsoup.connect(url) //connect to url
		//				.userAgent("Mozilla/5.0") //add a request header -> set user agent of connection
		//				.get();					  //set request method as GET and execute. 
		//		Elements elems = doc.getElementsByClass("sd_video_preview media-img mrg-end-lg span-3");	  //Extract only the webpage section containing info of the videos (a specific div class)
		//		System.out.println(elems);

//		//Parse URL HTML - get the divs and attributes giving the necessary info only of the 18 videos on DM trending page
//		String url = "http://www.dailymotion.com/en/trending/1"; //url of webpage to scrape
//
//		Document doc = Jsoup.connect(url) //connect to url
//				.userAgent("Mozilla/5.0") //add a request header -> set user agent of connection
//				.get();					  //set request method as GET and execute. 
//		Elements elemsVideos = doc.getElementsByClass("sd_video_preview media-img mrg-end-lg span-3");	  //Extract only the webpage section containing info of the videos (a specific div class)
//		Elements elemsVideoViews = doc.getElementsByClass("views");
//		
//		System.out.println("size : " + elemsVideos.size());
//		for (int i = 0; i < elemsVideos.size(); i++) {
//			String videoID = elemsVideos.get(i).attr("data-id"); //Video ID
//			String videoViews = elemsVideoViews.get(i).text(); //Video views
//			System.out.println(videoID);
//			System.out.println(videoViews);
//		}
		
		
		
		//Parse URL HTML - get the divs & attributes giving the necessary info only - non live AND live videos
		String url = "http://www.dailymotion.com/en/trending/1"; //url of webpage to scrape

		Document doc = Jsoup.connect(url) //connect to url
				.userAgent("Mozilla/5.0") //add a request header -> set user agent of connection
				.get();					  //set request method as GET and execute. 
		Elements elemsVideosNonLive = doc.getElementsByClass("sd_video_preview media-img mrg-end-lg span-3");	  //Extract only the webpage section containing info of the non-live videos (a specific div class)
		Elements elemsVideosLive = doc.getElementsByClass("sd_video_preview media-img mrg-end-lg span-3 no_wl"); //for live videos
		Elements elemsVideoViews = doc.getElementsByClass("views");
		
		System.out.println("size : " + elemsVideosNonLive.size());
		for (int i = 0; i < elemsVideosNonLive.size(); i++) {
			String videoID = elemsVideosNonLive.get(i).attr("data-id"); //Video ID
			String videoViews = elemsVideoViews.get(i).text(); //Video views
			System.out.println(videoID);
			System.out.println(videoViews);
		}
		
		for (int i = 0; i < elemsVideosLive.size(); i++) {
			String videoID = elemsVideosLive.get(i).attr("data-id");
			System.out.println(videoID);
		}
		
		System.out.println("total videos: " + (elemsVideosNonLive.size() + elemsVideosLive.size()));
	}
}
