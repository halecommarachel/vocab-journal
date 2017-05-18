package vocabjournal.storage;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Client for DynamoDB persistence layer for the Vocab Journal Skill
 */
public class VocabJournalDynamoDbClient {
    private final AmazonDynamoDBClient dynamoDBClient;

    public VocabJournalDynamoDbClient(final AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    public void saveItem(final VocabJournalUserDataItem item) {
        DynamoDBMapper mapper = createDynamoDbMapper();
        mapper.save(item);
    }

    public List<VocabJournalUserDataItem> loadItems(final VocabJournalUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDbMapper();

        DynamoDBQueryExpression<VocabJournalUserDataItem> query = new DynamoDBQueryExpression<>();
        query.withHashKeyValues(tableItem);
        PaginatedQueryList<VocabJournalUserDataItem> itemList = mapper.query(VocabJournalUserDataItem.class, query);
        return itemList;
    }

    public VocabJournalUserDataItem loadItem(final VocabJournalUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDbMapper();
        return mapper.load(tableItem);
    }

    public void deleteItem(final VocabJournalUserDataItem deleteItem) {
        DynamoDBMapper mapper = createDynamoDbMapper();
        mapper.delete(deleteItem);
    }

    /**
     * Creates a {@link DynamoDBMapper} with the default configuration
     * @return
     */
    private DynamoDBMapper createDynamoDbMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }


}
