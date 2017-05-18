package vocabjournal;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.JsonObject;
import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    public String lookupWord(String word) {
        String definition = null;
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
                JsonNode senseNode = sensesNode.get(0);
                JsonNode definitionsNode = senseNode.get("definitions");
                definition = definitionsNode.get(0).asText();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return definition;
    }

    public static void main(String[] args) {
        DictionaryClient client = new DictionaryClient();
        assert(client.lookupWord("fakkity") == null);
        assert(client.lookupWord("asperity").equals("harshness of tone or manner"));
        assert(client.lookupWord("objurgate").equals("rebuke severely; scold"));
    }
}
