package vocabjournal;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rachhale on 4/21/17.
 */
public final class VocabJournalSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<>();
    static {
        supportedApplicationIds.add("amzn1.ask.skill.3bba1b7a-4636-40f6-92ca-7682b3e8c178");
    }

    public VocabJournalSpeechletRequestStreamHandler() {
        super(new VocabJournalSpeechlet(), supportedApplicationIds);
    }
}
