package vocabjournal;

import vocabjournal.storage.VocabJournalEntryData;

/**
 * Contains session scoped settings
 */
public class SkillContext {
    private VocabJournalEntryData entryData;
    private boolean isTestMode;

    public VocabJournalEntryData getEntryData() {
        return entryData;
    }

    public void setEntryData(VocabJournalEntryData entryData) {
        this.entryData = entryData;
    }

    public boolean isTestMode() {
        return isTestMode;
    }

    public void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }
}
