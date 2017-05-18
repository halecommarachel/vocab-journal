package vocabjournal.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Model representing an item in the VocabJournalUserData DynamoDB table for the Vocab Journal Alexa Skill
 */
@DynamoDBTable(tableName = "VocabJournalUserData")
public class VocabJournalUserDataItem {
    private static final Logger log = LoggerFactory.getLogger(VocabJournalUserDataItem.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writerWithType(new TypeReference<VocabJournalEntryData>() {
    });

    private String customerId;
    private String word;
    private String definition;
    private int testCount;
//    private VocabJournalEntryData entryData;

    @DynamoDBHashKey(attributeName = "CustomerId")
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @DynamoDBRangeKey(attributeName = "Word")
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @DynamoDBAttribute(attributeName = "Definition")
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @DynamoDBAttribute(attributeName = "TestCount")
    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public void incrementTestCount() {
        testCount++;
    }

//    @DynamoDBAttribute(attributeName = "EntryData")
//    @DynamoDBMarshalling(marshallerClass = VocabJournalEntryDataMarshaller.class)
//    public VocabJournalEntryData getEntryData() {
//        return entryData;
//    }
//
//    public void setEntryData(VocabJournalEntryData entryData) {
//        this.entryData = entryData;
//    }

    /**
     * A {@link DynamoDBMarshaller} that provides marshalling and unmarshalling logic for a
     * {@link VocabJournalEntryData} value so that they can be persisted in the database as a String.
     */
    public static class VocabJournalEntryDataMarshaller implements
            DynamoDBMarshaller<VocabJournalEntryData> {

        @Override
        public String marshall(VocabJournalEntryData entryData) {
            try {

                String entryDataString = OBJECT_WRITER.writeValueAsString(entryData);
                log.debug("Marshalled entry data: " + entryDataString);
                return entryDataString;
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to marshall entry data", e);
            }
        }

        @Override
        public VocabJournalEntryData unmarshall(Class<VocabJournalEntryData> clazz, String json) {
            try {
                return OBJECT_MAPPER.readValue(json, new TypeReference<VocabJournalEntryData>(){});
            } catch (Exception e) {
                throw new IllegalStateException("Unable to unmarshall entry data value", e);
            }
        }
    }
}
