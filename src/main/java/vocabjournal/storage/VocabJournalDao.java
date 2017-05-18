package vocabjournal.storage;

import com.amazon.speech.speechlet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Contains the methods to interact with the persistence layer for Vocab Journal in DynamoDB
 */
public class VocabJournalDao {
    private final VocabJournalDynamoDbClient dynamoDbClient;
    private static final Logger log = LoggerFactory.getLogger(VocabJournalDao.class);

    public VocabJournalDao(VocabJournalDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void saveItem(Session session, String word, String definition) {
        VocabJournalUserDataItem item = new VocabJournalUserDataItem();
        item.setCustomerId(session.getUser().getUserId());
        item.setWord(word);
        item.setDefinition(definition);
        item.setTestCount(0);
        dynamoDbClient.saveItem(item);
    }

    private VocabJournalEntryData createVocabJournalEntryData(String word, String definition) {
        VocabJournalEntryData data = new VocabJournalEntryData();
        data.setWord(word);
        data.setDefinition(definition);
        data.setTestCount(0);
        return data;
    }

    public boolean containsCustomerWord(Session session, String word) {
        VocabJournalUserDataItem existingItem = getItem(session, word);
        return existingItem != null;
    }

    private VocabJournalUserDataItem getItem(Session session, String word) {
        // retrieve item for customerId, if it exists
        VocabJournalUserDataItem item = new VocabJournalUserDataItem();
        item.setCustomerId(session.getUser().getUserId());
        item.setWord(word);

        return dynamoDbClient.loadItem(item);
    }

    public VocabJournalUserDataItem getTestEntry(Session session) {
        List<VocabJournalUserDataItem> fetchedItems = getItems(session);
        if (fetchedItems != null && !fetchedItems.isEmpty()) {
            log.debug("Retrieved test entry list of size : " + fetchedItems.size());
            // sort list by testCount
            List<VocabJournalUserDataItem> allItems = new ArrayList<>();
            for (VocabJournalUserDataItem item : fetchedItems) {
                allItems.add(item);
            }
            Collections.sort(allItems, new Comparator<VocabJournalUserDataItem>() {
                @Override
                public int compare(VocabJournalUserDataItem o1, VocabJournalUserDataItem o2) {
                    return Integer.compare(o1.getTestCount(), o2.getTestCount());
                }
            });

            // Randomly select an item from the first MIN(4, array.length) elements of the array
            // This prevents a test word from occurring more often than 1 in 4 consecutive test words when there are
            // more than 4 words. When there are less than 4 words, the test word will be selected randomly
            int testWordIndex = (int) (Math.random() * Math.min(4, allItems.size()));
            VocabJournalUserDataItem testData = allItems.get(testWordIndex);
            log.debug("Retrieved test word : " + testData.getWord());
            // increment testCount and save back to dynamoDB
            testData.incrementTestCount();
            dynamoDbClient.saveItem(testData);

            return testData;
        }
        return null;
    }

    private List<VocabJournalUserDataItem> getItems(Session session) {
        VocabJournalUserDataItem item = new VocabJournalUserDataItem();
        item.setCustomerId(session.getUser().getUserId());

        return dynamoDbClient.loadItems(item);
    }

    public void deleteItem(Session session, String deleteWord) {
        VocabJournalUserDataItem deleteItem = new VocabJournalUserDataItem();
        deleteItem.setCustomerId(session.getUser().getUserId());
        deleteItem.setWord(deleteWord);
        dynamoDbClient.deleteItem(deleteItem);
    }
}
