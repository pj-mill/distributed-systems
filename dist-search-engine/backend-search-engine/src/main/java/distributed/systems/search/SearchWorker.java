package distributed.systems.search;


import distributed.systems.Constants;
import distributed.systems.models.DocumentData;
import distributed.systems.models.Result;
import distributed.systems.utils.SerializationUtils;
import distributed.systems.models.Task;
import distributed.systems.networking.OnRequestCallback;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchWorker implements OnRequestCallback {

    private static final Logger logger = LogManager.getLogger(SearchWorker.class);

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task) SerializationUtils.deserialize(requestPayload);
        Result result = createResult(task);
        return SerializationUtils.serialize(result);
    }

    private Result createResult(Task task) {
        List<String> documents = task.getDocuments();
        System.out.println(String.format("Received %d documents to process", documents.size()));

        Result result = new Result();

        for (String document : documents) {
            List<String> words = parseWordsFromDocument(document);
            DocumentData documentData = TFIDF.createDocumentData(words, task.getSearchTerms());
            result.addDocumentData(document, documentData);
        }
        return result;
    }

    private List<String> parseWordsFromDocument(String document) {
        try {
            FileReader fileReader = new FileReader(document);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromDocument(lines);

            bufferedReader.close();
            fileReader.close();

            return words;
        } 
        catch (FileNotFoundException e) {
            logger.error(e);
            return Collections.emptyList();
        } 
        catch (IOException e) {
            logger.error(e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getEndpoint() {
        return Constants.SEARCH_WORKER_ENDPOINT;
    }
}
