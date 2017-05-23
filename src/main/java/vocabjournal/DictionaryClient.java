package vocabjournal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

//import com.google.gson.JsonObject;

/**
 * Created by rachhale on 4/24/17.
 */
public class DictionaryClient {
    private final String APP_ID = "7c379e40";
    private final String APP_KEY = "a344aeb431aee3c9d743a629e860539f";
    private final String REQUEST_PATH = "https://od-api.oxforddictionaries.com/api/v1/entries/";
    private final String LANGUAGE = "en";

    private static DictionaryClient client;

    private DictionaryClient() {

    }

    public static DictionaryClient getInstance() {
        if (client == null) {
            client = new DictionaryClient();
        }
        return client;
    }

    public Deque<String> lookupWord(String word) {
        Deque<String> definitions = new ArrayDeque<>();
        word = word.toLowerCase();
        String urlString = REQUEST_PATH + LANGUAGE + "/" + word;
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("app_id", APP_ID);
            urlConnection.setRequestProperty("app_key", APP_KEY);

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(stringBuilder.toString());
                JsonNode resultsNode = rootNode.get("results");
                JsonNode resultNode = resultsNode.get(0);
                JsonNode lexicalEntriesNode = resultNode.get("lexicalEntries");
                JsonNode allEntriesNode = lexicalEntriesNode.get(0);
                JsonNode entriesNode = allEntriesNode.get("entries");
                JsonNode entryNode = entriesNode.get(0);
                JsonNode sensesNode = entryNode.get("senses");

                if (sensesNode.isArray()) {
                    for (final JsonNode senseNode : sensesNode) {
                        JsonNode definitionsNode = senseNode.get("definitions");
                        String definition = definitionsNode.get(0).asText();
                        definitions.add(definition);
                    }
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return definitions;
    }

    public static void main(String[] args) {
        DictionaryClient client = DictionaryClient.getInstance();
        client.lookupWord("abrasive");
        assert(client.lookupWord("fakkity") == null);
        assert(client.lookupWord("asperity").equals("harshness of tone or manner"));
        assert(client.lookupWord("objurgate").equals("rebuke severely; scold"));
    }
}
