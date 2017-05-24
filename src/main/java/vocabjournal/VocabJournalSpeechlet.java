package vocabjournal;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rachhale on 4/21/17.
 */
public class VocabJournalSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(VocabJournalSpeechlet.class);

    private AmazonDynamoDBClient amazonDynamoDBClient;
    private VocabJournalManager vocabJournalManager;

    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
        amazonDynamoDBClient = new AmazonDynamoDBClient();
        vocabJournalManager = new VocabJournalManager(amazonDynamoDBClient);
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", launchRequest.getRequestId(),
                session.getSessionId());

        return vocabJournalManager.getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
        Intent intent = intentRequest.getIntent();
        String intentName = intent.getName();
        log.info("onIntent requestId={}, sessionId={}, intent={}", intentRequest.getRequestId(), session.getSessionId(), intentName);

        if ("AddWordIntent".equals(intentName)) {
            return vocabJournalManager.getAddWordIntentResponse(session, intent);
        } else if ("VocabTestIntent".equals(intentName)) {
            return vocabJournalManager.getTestIntentResponse(session, intent);
        } else if ("TestTypeIntent".equals(intentName)) {
            return vocabJournalManager.getTestTypeIntentResponse(session, intent);
        } else if ("WordTestAnswerIntent".equals(intentName)) {
            return vocabJournalManager.getWordTestAnswerIntentResponse(session, intent);
        } else if ("DefinitionTestAnswerIntent".equals(intentName)) {
            return vocabJournalManager.getDefinitionTestAnswerIntentResponse(session, intent);
        } else if ("DeleteWordIntent".equals(intentName)) {
            return vocabJournalManager.deleteWordIntentResponse(session, intent);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return vocabJournalManager.getHelpIntentResponse();
        } else if ("AMAZON.StopIntent".equals(intentName) || "AMAZON.CancelIntent".equals(intentName)) {
            return vocabJournalManager.getStopIntentResponse();
        } else if ("AMAZON.YesIntent".equals(intentName)) {
            return vocabJournalManager.getDefinitionYesIntentResponse(session);
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            return vocabJournalManager.getDefinitionNoResponse(session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }
}
