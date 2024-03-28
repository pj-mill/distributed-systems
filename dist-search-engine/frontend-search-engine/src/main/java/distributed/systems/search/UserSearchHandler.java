package distributed.systems.search;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import com.google.protobuf.InvalidProtocolBufferException;

import distributed.systems.cluster.management.ServiceRegistry;
import distributed.systems.models.frontend.FrontendSearchRequest;
import distributed.systems.models.frontend.FrontendSearchResponse;
import distributed.systems.models.proto.SearchModel;
import distributed.systems.networking.OnRequestCallback;
import distributed.systems.networking.WebClient;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserSearchHandler implements OnRequestCallback {
    private static final String ENDPOINT = "/documents_search";
    private static final String DOCUMENTS_LOCATION = "C:\\Devs\\Courses New\\distributed-systems\\dist-search-engine\\resources\\books";
    private final ObjectMapper objectMapper;
    private final WebClient client;
    private final ServiceRegistry searchCoordinatorRegistry;

    private static final Logger logger = LogManager.getLogger(UserSearchHandler.class);

    public UserSearchHandler(ServiceRegistry searchCoordinatorRegistry) {
        this.searchCoordinatorRegistry = searchCoordinatorRegistry;
        this.client = new WebClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        try {
            FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(requestPayload,
                    FrontendSearchRequest.class);

            logger.info("Received search request: " + frontendSearchRequest.getSearchQuery());
            FrontendSearchResponse frontendSearchResponse = createFrontendResponse(frontendSearchRequest);

            return objectMapper.writeValueAsBytes(frontendSearchResponse);
        } catch (IOException e) {
            logger.error("Failed to handle request", e);
            return new byte[0];
        }
    }

    private FrontendSearchResponse createFrontendResponse(FrontendSearchRequest frontendSearchRequest) {
        SearchModel.Response searchClusterResponse = sendRequestToSearchCluster(frontendSearchRequest.getSearchQuery());

        List<FrontendSearchResponse.SearchResultInfo> filteredResults = filterResults(searchClusterResponse,
                frontendSearchRequest.getMaxNumberOfResults(),
                frontendSearchRequest.getMinScore());

        return new FrontendSearchResponse(filteredResults, DOCUMENTS_LOCATION);
    }

    private List<FrontendSearchResponse.SearchResultInfo> filterResults(SearchModel.Response searchClusterResponse,
            long maxResults,
            double minScore) {

        double maxScore = getMaxScore(searchClusterResponse);

        List<FrontendSearchResponse.SearchResultInfo> searchResultInfoList = new ArrayList<>();

        for (int i = 0; i < searchClusterResponse.getRelevantDocumentsCount() && i < maxResults; i++) {

            int normalizedDocumentScore = normalizeScore(searchClusterResponse.getRelevantDocuments(i).getScore(),
                    maxScore);
            if (normalizedDocumentScore < minScore) {
                continue; // break in the lecture is an error
            }

            String documentName = searchClusterResponse.getRelevantDocuments(i).getDocumentName();

            String title = getDocumentTitle(documentName);
            String extension = getDocumentExtension(documentName);

            FrontendSearchResponse.SearchResultInfo resultInfo = new FrontendSearchResponse.SearchResultInfo(title,
                    extension, normalizedDocumentScore);

            searchResultInfoList.add(resultInfo);
        }

        return searchResultInfoList;
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    private static String getDocumentExtension(String document) {
        String[] parts = document.split("\\.");
        if (parts.length == 2) {
            return parts[1];
        }
        return "";
    }

    private static String getDocumentTitle(String document) {
        return document.split("\\.")[0];
    }

    private static int normalizeScore(double inputScore, double maxScore) {
        return (int) Math.ceil(inputScore * 100.0 / maxScore);
    }

    private static double getMaxScore(SearchModel.Response searchClusterResponse) {
        if (searchClusterResponse.getRelevantDocumentsCount() == 0) {
            return 0;
        }
        return searchClusterResponse.getRelevantDocumentsList()
                .stream()
                .map(document -> document.getScore())
                .max(Double::compareTo)
                .get();
    }

    private SearchModel.Response sendRequestToSearchCluster(String searchQuery) {
        SearchModel.Request searchRequest = SearchModel.Request.newBuilder()
                .setSearchQuery(searchQuery)
                .build();

        try {
            String coordinatorAddress = searchCoordinatorRegistry.getRandomServiceAddress();
            if (coordinatorAddress == null) {
                logger.error("Search Cluster Coordinator is unavailable");
                return SearchModel.Response.getDefaultInstance();
            }

            byte[] payloadBody = client.sendTask(coordinatorAddress, searchRequest.toByteArray()).join();

            return SearchModel.Response.parseFrom(payloadBody);
        } catch (InterruptedException | KeeperException | InvalidProtocolBufferException e) {
            logger.error("Failed to send request to search cluster", e);
            return SearchModel.Response.getDefaultInstance();
        }
    }
}
