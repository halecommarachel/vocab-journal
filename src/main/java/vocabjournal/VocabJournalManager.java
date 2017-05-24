package vocabjournal;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vocabjournal.storage.VocabJournalDao;
import vocabjournal.storage.VocabJournalDynamoDbClient;
import vocabjournal.storage.VocabJournalUserDataItem;

import java.util.Deque;
import java.util.Map;

/**
 * Creates SpeechletResponses for Vocab Journal intents
 */
public class VocabJournalManager {
    private static final Logger log = LoggerFactory.getLogger(VocabJournalManager.class);
    private static final String SLOT_NEW_WORD = "newWord";
    private static final String SLOT_DEFINITION = "definition";
    private static final String SLOT_ANSWER_WORD = "answerWord";
    private static final String SLOT_DELETE_WORD = "deleteWord";
    private static final String SLOT_TEST_TYPE = "testType";
    private static final String WORD_ATTRIBUTE = "word";
    private static final String DEFINITION_ATTRIBUTE = "definition";
    private static final String DEFINITION_DEQUE_ATTRIBUTE = "definitionDeque";

    private final DictionaryClient dictionaryClient = DictionaryClient.getInstance();
    private final VocabJournalDao vocabJournalDao;


    public VocabJournalManager(final AmazonDynamoDBClient amazonDynamoDBClient) {
        VocabJournalDynamoDbClient dynamoDbClient = new VocabJournalDynamoDbClient(amazonDynamoDBClient);
        vocabJournalDao = new VocabJournalDao(dynamoDbClient);
    }

    public SpeechletResponse getWelcomeResponse() {
        SimpleCard card = new SimpleCard();
        card.setTitle(VocabJournalTextUtil.WELCOME_CARD_TITLE);
        card.setContent(VocabJournalTextUtil.STARTUP_HELP_CARD_CONTENT);
        return getNewAskResponseSameReprompt(VocabJournalTextUtil.STARTUP_HELP_SSML, true, card);
    }

    public SpeechletResponse getAddWordIntentResponse (Session session, Intent intent) {
        String newWord = intent.getSlot(SLOT_NEW_WORD).getValue();
        if (newWord == null || newWord.isEmpty() || newWord.contains(" ")) {
            return getNewAskResponseSameReprompt(VocabJournalTextUtil.INVALID_INPUT_ADD_WORD_HELP, false, null);
        } else {
            if (vocabJournalDao.containsCustomerWord(session, newWord)) {
                String content = String.format(VocabJournalTextUtil.ADD_EXISTING_WORD_FORMAT, newWord);
                return getNewTellResponseSameReprompt(content, false, null);
            }
            Deque<String> definitionDeque = dictionaryClient.lookupWord(newWord);
            if (definitionDeque == null) {
                return getNewTellResponseSameReprompt(VocabJournalTextUtil.INVALID_WORD_HELP, false, null);
            } else {
                session.setAttribute(WORD_ATTRIBUTE, newWord);
                String firstDefinition = definitionDeque.removeFirst();
                session.setAttribute(DEFINITION_ATTRIBUTE, firstDefinition);

                if (definitionDeque.isEmpty()) {
                    // There is only one definition, save this
                    return getDefinitionYesIntentResponse(session);
                }
                session.setAttribute(DEFINITION_DEQUE_ATTRIBUTE, definitionDeque);
                String content = String.format(VocabJournalTextUtil.MULTIPLE_DEFINITION_FORMAT, newWord) + String.format(VocabJournalTextUtil.MULTIPLE_DEFINITION_QUERY_FORMAT, firstDefinition);

                return getNewAskResponseSameReprompt(content, false, null);
            }
        }
    }

    public SpeechletResponse getDefinitionYesIntentResponse(Session session) {
        String word = (String) session.getAttribute(WORD_ATTRIBUTE);
        String definition = (String) session.getAttribute(DEFINITION_ATTRIBUTE);
        log.debug(String.format("Adding %s : %s", word, word));
        // store word and definition
        vocabJournalDao.saveItem(session, word, definition);

        String speechContent = String.format(VocabJournalTextUtil.ADDED_WORD_FORMAT, word, definition);

        SimpleCard card = new SimpleCard();
        String cardContent = String.format(VocabJournalTextUtil.ADDED_WORD_CARD_CONTENT_FORMAT, word, definition);
        card.setTitle(String.format(VocabJournalTextUtil.ADDED_WORD_CARD_TITLE_FORMAT, word));
        card.setContent(cardContent);

        return getNewTellResponseSameReprompt(speechContent, false, card);
    }

    public SpeechletResponse getDefinitionNoResponse(Session session) {
        String word = (String) session.getAttribute(WORD_ATTRIBUTE);
        Deque<String> definitionDeque = (Deque<String>) session.getAttribute(DEFINITION_DEQUE_ATTRIBUTE);
        if (definitionDeque.isEmpty()) {
            session.removeAttribute(WORD_ATTRIBUTE);
            session.removeAttribute(DEFINITION_ATTRIBUTE);
            session.removeAttribute(DEFINITION_DEQUE_ATTRIBUTE);
            String content = String.format(VocabJournalTextUtil.NO_MORE_DEFINITIONS_FORMAT, word);
            return getNewTellResponseSameReprompt(content, false, null);
        }
        String nextDefinition = definitionDeque.removeFirst();
        session.setAttribute(DEFINITION_ATTRIBUTE, nextDefinition);

        String content = String.format(VocabJournalTextUtil.MULTIPLE_DEFINITION_QUERY_FORMAT, nextDefinition);
        return getNewAskResponseSameReprompt(content, false, null);
    }

    public SpeechletResponse getTestIntentResponse(Session session, Intent intent) {
        if (intent.getSlots().containsKey(SLOT_TEST_TYPE)) {
            String type = intent.getSlot(SLOT_TEST_TYPE).getValue();
            if ("word".equals(type)) {
                return getWordTestIntentResponse(session);
            } else if ("definition".equals(type)) {
                return getDefinitionTestIntentResponse(session);
            }
        }
        return getNewAskResponseSameReprompt(VocabJournalTextUtil.TEST_TYPE_REQUEST, false, null);
    }

    public SpeechletResponse getTestTypeIntentResponse(Session session, Intent intent) {
        if (intent.getSlots().containsKey(SLOT_TEST_TYPE)) {
            String type = intent.getSlot(SLOT_TEST_TYPE).getValue();
            if ("word".equals(type)) {
                return getWordTestIntentResponse(session);
            } else if ("definition".equals(type)) {
                return getDefinitionTestIntentResponse(session);
            }
        }
        return getNewAskResponseSameReprompt(VocabJournalTextUtil.TEST_TYPE_REQUEST, false, null);
    }

    public SpeechletResponse getDefinitionTestIntentResponse(Session session) {
        // fetch test word and construct speech content
        VocabJournalUserDataItem dataItem = vocabJournalDao.getTestEntry(session);
        if (dataItem == null) {
            return getNewTellResponseSameReprompt(VocabJournalTextUtil.NO_WORDS_TEST_HELP, false, null);
        } else {
            session.setAttribute(WORD_ATTRIBUTE, dataItem.getWord());
            session.setAttribute(DEFINITION_ATTRIBUTE, dataItem.getDefinition());
            String testWord = dataItem.getWord();
            String content = String.format(VocabJournalTextUtil.DEFINITION_TEST_FORMAT, testWord);
            return getNewAskResponseSameReprompt(content, false, createSimpleCard(content, VocabJournalTextUtil.DEFINITION_TEST_CARD_TITLE));
        }
    }

    public SpeechletResponse getDefinitionTestAnswerIntentResponse(Session session, Intent intent) {
        if (session.getAttributes().containsKey(WORD_ATTRIBUTE)) {
            if (intent.getSlots().containsKey(SLOT_DEFINITION)) {
                String word = (String) session.getAttribute(WORD_ATTRIBUTE);
                String definition = (String) session.getAttribute(DEFINITION_ATTRIBUTE);
                String content;
                SimpleCard simpleCard = new SimpleCard();
                if (userKnowsAnswer(session, intent)) {
                    content = String.format(VocabJournalTextUtil.CORRECT_DEFINITION_TEST_ANSWER_FORMAT, word);
                    simpleCard.setTitle(VocabJournalTextUtil.CORRECT_ANSWER_CARD_TITLE);
                } else {
                    content = String.format(VocabJournalTextUtil.INCORRECT_DEFINITION_TEST_ANSWER_FORMAT, word, definition);
                    simpleCard.setTitle(VocabJournalTextUtil.WRONG_ANSWER_CARD_TITLE);
                }
                simpleCard.setContent(content);
                return getNewTellResponseSameReprompt(content, false, simpleCard);
            } else {
                String word = (String) session.getAttribute(WORD_ATTRIBUTE);
                String content = String.format(VocabJournalTextUtil.DEFINITION_TEST_FORMAT, word) + VocabJournalTextUtil.DEFINITION_TEST_HELP;
                return getNewAskResponseSameReprompt(content, false, null);
            }
        } else {
            return getNewAskResponseSameReprompt(VocabJournalTextUtil.DEFINITION_TEST_HELP, false, null);
        }
    }

    public SpeechletResponse getWordTestIntentResponse(Session session) {
        // fetch test word and construct speech content
        VocabJournalUserDataItem dataItem = vocabJournalDao.getTestEntry(session);
        if (dataItem == null) {
            return getNewTellResponseSameReprompt(VocabJournalTextUtil.NO_WORDS_TEST_HELP, false, null);
        } else {
            session.setAttribute(WORD_ATTRIBUTE, dataItem.getWord());
            session.setAttribute(DEFINITION_ATTRIBUTE, dataItem.getDefinition());
            String testDefinition = dataItem.getDefinition();
            String content = String.format(VocabJournalTextUtil.WORD_TEST_FORMAT, testDefinition);
            return getNewAskResponseSameReprompt(content, false, createSimpleCard(content, VocabJournalTextUtil.WORD_TEST_CARD_TITLE));
        }
    }

    public SpeechletResponse getWordTestAnswerIntentResponse(Session session, Intent intent) {
        if (session.getAttributes().containsKey(WORD_ATTRIBUTE)) {
            if (intent.getSlots().containsKey(SLOT_ANSWER_WORD)) {
                String word = (String) session.getAttribute(WORD_ATTRIBUTE);
                String content;
                SimpleCard card = new SimpleCard();
                if (userKnowsReverseAnswer(session, intent)) {
                    content = String.format(VocabJournalTextUtil.CORRECT_WORD_TEST_ANSWER_FORMAT, word);
                    card.setTitle(VocabJournalTextUtil.CORRECT_ANSWER_CARD_TITLE);
                } else {
                    content = String.format(VocabJournalTextUtil.INCORRECT_WORD_TEST_ANSWER_FORMAT, word);
                    card.setTitle(VocabJournalTextUtil.WRONG_ANSWER_CARD_TITLE);
                }
                card.setContent(content);
                return getNewTellResponseSameReprompt(content, false, card);
            } else {
                String definition = (String) session.getAttribute(DEFINITION_ATTRIBUTE);
                String content = String.format(VocabJournalTextUtil.WORD_TEST_FORMAT, definition) + VocabJournalTextUtil.WORD_TEST_HELP;

                return getNewAskResponseSameReprompt(content, false, null);
            }
        } else {
            return getNewAskResponseSameReprompt(VocabJournalTextUtil.WORD_TEST_HELP, false, null);
        }
    }

    private boolean userKnowsReverseAnswer(Session session, Intent intent) {
        Slot answerWordSlot = intent.getSlot(SLOT_ANSWER_WORD);
        if (answerWordSlot != null) {
            String userAnswerWord = answerWordSlot.getValue();
            Map<String, Object> attributes = session.getAttributes();
            if (attributes.containsKey(WORD_ATTRIBUTE)) {
                if (userAnswerWord.equals(attributes.get(WORD_ATTRIBUTE))) {
                    return true;
                }
            }
        }
        return false;
    }

    public SpeechletResponse getHelpIntentResponse() {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(VocabJournalTextUtil.COMPLETE_HELP);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    public SpeechletResponse getStopIntentResponse() {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText("");
        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    public SpeechletResponse deleteWordIntentResponse(Session session, Intent intent) {
        String deleteWord = intent.getSlot(SLOT_DELETE_WORD).getValue();
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        if (deleteWord == null || deleteWord.isEmpty() || deleteWord.contains(" ")) {
            speech.setText(VocabJournalTextUtil.INVALID_INPUT_DELETE_WORD_HELP);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);
            return SpeechletResponse.newAskResponse(speech, reprompt);
        } else {
            if (vocabJournalDao.containsCustomerWord(session, deleteWord)) {
                vocabJournalDao.deleteItem(session, deleteWord);
                speech.setText(String.format(VocabJournalTextUtil.DELETED_WORD_FORMAT, deleteWord));
            } else {
                speech.setText(String.format(VocabJournalTextUtil.DELETE_WORD_NOT_PRESENT_FORMAT, deleteWord));
            }
            return SpeechletResponse.newTellResponse(speech);
        }
    }

    private SpeechletResponse getNewTellResponseSameReprompt(String text, boolean isSSML, Card card) {
        if (isSSML) {
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml(text);

            if (card != null) {
                return SpeechletResponse.newTellResponse(outputSpeech, card);
            }
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText(text);

            if (card != null) {
                return SpeechletResponse.newTellResponse(outputSpeech, card);
            }
            return SpeechletResponse.newTellResponse(outputSpeech);
        }
    }

    private Card createSimpleCard(String content, String title) {
        SimpleCard card = new SimpleCard();
        if (content != null) {
            card.setContent(content);
        }
        if (title != null) {
            card.setTitle(title);
        }
        return card;
    }

    private SpeechletResponse getNewAskResponseSameReprompt(String text, boolean isSSML, Card card) {
        if (isSSML) {
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml(text);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(outputSpeech);

            if (card != null) {
                return SpeechletResponse.newAskResponse(outputSpeech, reprompt, card);
            }
            return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
        } else {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText(text);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(outputSpeech);

            if (card != null) {
                return SpeechletResponse.newAskResponse(outputSpeech, reprompt, card);
            }
            return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
        }
    }

    private boolean userKnowsAnswer(Session session, Intent intent) {
        Slot definitionSlot = intent.getSlot(SLOT_DEFINITION);
        if (definitionSlot != null) {
            String userDefinition = definitionSlot.getValue();
            Map<String, Object> attributes = session.getAttributes();
            String correctDefinition = (String) attributes.get(DEFINITION_ATTRIBUTE);
            // remove punctuation
            correctDefinition = correctDefinition.replaceAll("[.,;]", "");
            log.debug("Correct definition, no punc [{}], user definition [{}]", correctDefinition, userDefinition);
            if (attributes.containsKey(DEFINITION_ATTRIBUTE)) {
                if (userDefinition.equals(correctDefinition)) {
                    return true;
                }
            }
        }
        return false;
    }
}
