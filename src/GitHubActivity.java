import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubActivity {
    private String username;
    private String apiUrl;

    public GitHubActivity(String username) {
        this.username = username;
        this.apiUrl = "https://api.github.com/users/" + username + "/events";
    }

    public void fetchAndDisplayActivity() {
        try {
            URL url = new URI(apiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Java-CLI");

            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                System.out.println("Error: User not found.");
                return;
            } else if (responseCode != 200) {
                System.out.println("Error: Unable to fetch data (Response Code: " + responseCode + ")");
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            displayActivity(response.toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void displayActivity(String jsonResponse) {
        String[] events = jsonResponse.split("\\{\"event\":");

        if (events.length <= 1) {
            System.out.println("No recent activity found.");
            return;
        }

        System.out.println("Recent GitHub Activity:");
        Pattern pattern = Pattern.compile("\"type\":\"(.*?)\".*?\"repo\":\\{\"name\":\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonResponse);
        
        int count = 0;
        while (matcher.find() && count < 5) { // Limit to 5 recent activities
            String eventType = matcher.group(1);
            String repoName = matcher.group(2);
            
            switch (eventType) {
                case "PushEvent":
                    System.out.println("- Pushed to " + repoName);
                    break;
                case "IssuesEvent":
                    System.out.println("- Opened an issue in " + repoName);
                    break;
                case "WatchEvent":
                    System.out.println("- Starred " + repoName);
                    break;
                default:
                    System.out.println("- " + eventType + " in " + repoName);
            }
            count++;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: github-activity <username>");
            return;
        }
        
        GitHubActivity activity = new GitHubActivity(args[0]);
        activity.fetchAndDisplayActivity();
    }
}
