package vocabjournal.storage;

/**
 * Contains word and its definition
 */
public class VocabJournalEntryData {
    private String word;
    private String definition;
    private int testCount;

    public VocabJournalEntryData() {
        // public no-arg constructor required for DynamoDBMapper marshalling
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public void incrementTestCount() {
        testCount++;
    }

    @Override
    public String toString() {
        return "VocabJournalEntryData{" +
                "word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                ", testCount=" + testCount +
                '}';
    }
}
