package distributed.systems;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import distributed.systems.models.DocumentData;
import distributed.systems.search.TFIDF;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    private static final Logger logger = LogManager.getLogger(AppTest.class);
    public static final String SEARCH_QUERY_1 = "The best detective that catches many criminals using his deductive methods";
    public static final String SEARCH_QUERY_2 = "The girl that falls through a rabbit hole into a fantasy wonderland";

    @Test
    public void shouldBeAliceInWonderland() throws IOException
    {
        File documentsDirectoy = new File(constants.RESOURCES_LOCATION);
        List<String> documents = Arrays.asList(documentsDirectoy.list()).stream().map(documentName -> documentsDirectoy + "\\" + documentName).collect(Collectors.toList());

        List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_2);
        
        String mostRelevantDocument = findMostRelevantDocuments(documents, terms );

        final String expectedResult = "Alice’s Adventures in Wonderland.txt";

        assertTrue( mostRelevantDocument.contains(expectedResult));
    }

    @Test
    public void shouldBeSherlockHolmes() throws IOException
    {
        File documentsDirectoy = new File(constants.RESOURCES_LOCATION);
        List<String> documents = Arrays.asList(documentsDirectoy.list()).stream().map(documentName -> documentsDirectoy + "\\" + documentName).collect(Collectors.toList());

        List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_1);
        
        String mostRelevantDocument = findMostRelevantDocuments(documents, terms );

        final String expectedResult = "The Adventures of Sherlock Holmes.txt";

        assertTrue( mostRelevantDocument.contains(expectedResult));
    }

    private static String findMostRelevantDocuments(List<String> documents, List<String> terms) throws IOException {
        Map<String, DocumentData> documentResults = new HashMap<>();

        for (String document : documents) {
            BufferedReader reader = new BufferedReader(new FileReader(document));
            List<String> lines = reader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromDocument(lines);
        
            DocumentData documentData = TFIDF.createDocumentData(words, terms);
            documentResults.put(document, documentData);

            reader.close();
        }

        Map<Double, List<String>> documentsScores = TFIDF.getDocumentsScores(terms, documentResults);
        Double score = documentsScores.keySet().iterator().next();
        List<String> documentsWithCurrentScore = documentsScores.get(score);
        String firstDocument = documentsWithCurrentScore.get(0);
        logger.info("Most Relevant Document: " + firstDocument + " Score: " + score + " Terms: " + terms);

        return firstDocument;

        // for (Double score : documentsScores.keySet()) {
        //     List<String> documentsWithCurrentScore = documentsScores.get(score);
        //     String firstDocument = documentsWithCurrentScore.get(0);
        //     logger.info("Document: " + firstDocument + " Score: " + score + " Terms: " + terms);

        //     // for (String document : documentsWithCurrentScore) {
        //     //     logger.info("Document: " + document + " Score: " + score + " Terms: " + terms);
        //     // }
        // }
    }
}
