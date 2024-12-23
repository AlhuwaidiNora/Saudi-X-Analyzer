import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SaudiTrendingAnalyzer {
    private static final String SAUDI_REGION = "SA";
    private List<Tweet> tweets;
    private Map<String, Integer> hashtagCount;
    private LocalDateTime lastUpdateTime;

    public SaudiTrendingAnalyzer() {
        this.tweets = new ArrayList<>();
        this.hashtagCount = new HashMap<>();
        this.lastUpdateTime = LocalDateTime.now();
    }

    // Represents a single tweet
    public static class Tweet {
        private String content;
        private LocalDateTime timestamp;
        private String location;
        private List<String> hashtags;

        public Tweet(String content, String location) {
            this.content = content;
            this.timestamp = LocalDateTime.now();
            this.location = location;
            this.hashtags = extractHashtags(content);
        }

        private List<String> extractHashtags(String content) {
            List<String> hashtags = new ArrayList<>();
            Pattern pattern = Pattern.compile("#\\w+");
            Matcher matcher = pattern.matcher(content);
            
            while (matcher.find()) {
                hashtags.add(matcher.group().toLowerCase());
            }
            return hashtags;
        }

        public List<String> getHashtags() {
            return hashtags;
        }

        public String getLocation() {
            return location;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    // Add a new tweet to the analysis
    public void addTweet(Tweet tweet) {
        if (tweet.getLocation().equals(SAUDI_REGION)) {
            tweets.add(tweet);
            updateHashtagCounts(tweet.getHashtags());
        }
    }

    // Update hashtag frequency counts
    private void updateHashtagCounts(List<String> hashtags) {
        for (String hashtag : hashtags) {
            hashtagCount.merge(hashtag, 1, Integer::sum);
        }
    }

    // Get top trending hashtags
    public List<Map.Entry<String, Integer>> getTopTrends(int limit) {
        return hashtagCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Get trending hashtags within a specific time window (in hours)
    public List<Map.Entry<String, Integer>> getTrendsInTimeWindow(int hours, int limit) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        Map<String, Integer> recentHashtags = new HashMap<>();
        
        tweets.stream()
                .filter(tweet -> tweet.getTimestamp().isAfter(cutoffTime))
                .forEach(tweet -> {
                    tweet.getHashtags().forEach(hashtag ->
                            recentHashtags.merge(hashtag, 1, Integer::sum));
                });

        return recentHashtags.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Calculate hashtag growth rate (comparing current hour to previous hour)
    public Map<String, Double> getHashtagGrowthRates() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        
        Map<String, Integer> currentHourCounts = new HashMap<>();
        Map<String, Integer> previousHourCounts = new HashMap<>();
        Map<String, Double> growthRates = new HashMap<>();

        // Count hashtags in current hour
        tweets.stream()
                .filter(tweet -> tweet.getTimestamp().isAfter(oneHourAgo))
                .forEach(tweet -> {
                    tweet.getHashtags().forEach(hashtag ->
                            currentHourCounts.merge(hashtag, 1, Integer::sum));
                });

        // Count hashtags in previous hour
        tweets.stream()
                .filter(tweet -> tweet.getTimestamp().isAfter(twoHoursAgo) 
                             && tweet.getTimestamp().isBefore(oneHourAgo))
                .forEach(tweet -> {
                    tweet.getHashtags().forEach(hashtag ->
                            previousHourCounts.merge(hashtag, 1, Integer::sum));
                });

        // Calculate growth rates
        currentHourCounts.forEach((hashtag, currentCount) -> {
            int previousCount = previousHourCounts.getOrDefault(hashtag, 0);
            double growthRate = previousCount == 0 ? currentCount : 
                    ((double) currentCount - previousCount) / previousCount * 100;
            growthRates.put(hashtag, growthRate);
        });

        return growthRates;
    }
}
