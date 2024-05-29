import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CricketDataAnalysis {

    public static void main(String[] args) {
        try {
            // Fetch data from the API
            String url = "https://api.cuvora.com/car/partner/cricket-data";
            String apiKey = "test-creds@2320";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apiKey", apiKey);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // Print the raw response for debugging
            String response = content.toString();
            System.out.println("API Response: " + response);

            // Parse JSON response
            Object json = new JSONTokener(response).nextValue();
            JSONArray matches;

            if (json instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) json;
                // Look for the matches array in the JSON object
                if (jsonObject.has("matches")) {
                    matches = jsonObject.getJSONArray("matches");
                } else {
                    throw new Exception("Unexpected JSON format: 'matches' key not found");
                }
            } else if (json instanceof JSONArray) {
                matches = (JSONArray) json;
            } else {
                throw new Exception("Unexpected JSON format");
            }

            // Variables to hold required results
            int highestScore = 0;
            String highestScoreTeam = "";
            int matchesWith300PlusScore = 0;

            // Iterate through each match
            for (int i = 0; i < matches.length(); i++) {
                JSONObject match = matches.getJSONObject(i);

                String matchStatus = match.getString("ms");
                if (!matchStatus.equals("Result")) {
                    continue; // Skip if the match result is not yet obtained
                }

                int t1Score = extractScore(match.getString("t1s"));
                int t2Score = extractScore(match.getString("t2s"));

                // Check for highest score in one innings
                if (t1Score > highestScore) {
                    highestScore = t1Score;
                    highestScoreTeam = match.getString("t1");
                }
                if (t2Score > highestScore) {
                    highestScore = t2Score;
                    highestScoreTeam = match.getString("t2");
                }

                // Check if total score of both teams is 300 or more
                if (t1Score + t2Score >= 300) {
                    matchesWith300PlusScore++;
                }
            }

            // Print and return the results
            String result = String.format("Highest Score: %d and Team Name is: %s\nNumber Of Matches with total 300 Plus Score: %d",
                    highestScore, highestScoreTeam, matchesWith300PlusScore);
            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility function to extract integer score from string
    private static int extractScore(String score) {
        if (score == null || score.isEmpty() || !score.matches("\\d+/\\d*")) {
            return 0;
        }
        String[] parts = score.split("/");
        return Integer.parseInt(parts[0]);
    }
}
