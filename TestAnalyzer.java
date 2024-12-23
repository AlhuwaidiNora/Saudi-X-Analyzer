public class TestAnalyzer {
    public static void main(String[] args) {
        SaudiTrendingAnalyzer analyzer = new SaudiTrendingAnalyzer();
        
        // Add test tweets
        analyzer.addTweet(new SaudiTrendingAnalyzer.Tweet("Test tweet #Riyadh #Tech", "SA"));
        analyzer.addTweet(new SaudiTrendingAnalyzer.Tweet("Another tweet #Jeddah #Tech", "SA"));
        
        // Print top trends
        System.out.println("Top Trends:");
        analyzer.getTopTrends(5).forEach(entry -> 
            System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}
