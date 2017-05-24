package vocabjournal;

/**
 * Contains text constants
 */
public class VocabJournalTextUtil {
    public static final String STARTUP_HELP_SSML = "<speak>Welcome to <phoneme alphabet=\"ipa\" ph=\"'voʊ kæb\">Vocab</phoneme> Journal. You can add a word, start a test, or delete a word. What would you like to do? </speak> ";
    public static final String STARTUP_HELP_CARD_CONTENT = "Welcome to Vocab Journal. You can add a word, start a test, or delete a word. What would you like to do?";
    public static final String COMPLETE_HELP = "Here are some things you can say. Add chagrin. Start a word test. Start a definition test. Delete chagrin" +
            " You can also say, stop, if you're done. So, how can I help?";
    public static final String DEFINITION_TEST_HELP = "Start your response with, the definition is.";
    public static final String WORD_TEST_HELP = "Start your response with, the word is";
    public static final String INVALID_WORD_HELP = "Entries must be a word from the Oxford Dictionary.";
    public static final String INVALID_INPUT_ADD_WORD_HELP = "To add a word, like chagrin for example, you can say, add chagrin.";
    public static final String INVALID_INPUT_DELETE_WORD_HELP = "To delete a word, like chagrin for example, you can say, delete chagrin.";
    public static final String DELETE_WORD_NOT_PRESENT_FORMAT = "%s is not present in your journal.";
    public static final String ADD_EXISTING_WORD_FORMAT = "%s is already in your journal.";
    public static final String DELETED_WORD_FORMAT = "Deleted %s from your journal.";
    public static final String NO_WORDS_TEST_HELP = "Please add a word before beginning a test. For example, you can say, add chagrin.";
    public static final String ADDED_WORD_FORMAT = "Added %s, %s, to your journal";
    public static final String DEFINITION_TEST_FORMAT = "What is the definition of: %s.";
    public static final String WORD_TEST_FORMAT = "What word corresponds to this definition? %s.";
    public static final String CORRECT_DEFINITION_TEST_ANSWER_FORMAT = "You learned %s!";
    public static final String CORRECT_WORD_TEST_ANSWER_FORMAT = "%s is correct!";
    public static final String INCORRECT_DEFINITION_TEST_ANSWER_FORMAT = "%s means %s.";
    public static final String INCORRECT_WORD_TEST_ANSWER_FORMAT = "The correct word is %s.";
    public static final String WELCOME_CARD_TITLE = "Welcome to Vocab Journal";
    public static final String ADDED_WORD_CARD_TITLE_FORMAT = "Added %s to your journal";
    public static final String ADDED_WORD_CARD_CONTENT_FORMAT = "%s:%s";
    public static final String TEST_TYPE_REQUEST = "You can start a word test, a definition test, or get help. What would you like to do?";
    public static final String DEFINITION_TEST_CARD_TITLE = "Definition Test";
    public static final String WORD_TEST_CARD_TITLE = "Word Test";
    public static final String WRONG_ANSWER_CARD_TITLE = "Keeping Trying";
    public static final String CORRECT_ANSWER_CARD_TITLE = "Correct!";
    public static final String MULTIPLE_DEFINITION_FORMAT = "%s has more than one definition. ";
    public static final String MULTIPLE_DEFINITION_QUERY_FORMAT = "Would you like to use this definition? %s";
    public static final String NO_MORE_DEFINITIONS_FORMAT = "There are no more definitions available for %s.";
}
