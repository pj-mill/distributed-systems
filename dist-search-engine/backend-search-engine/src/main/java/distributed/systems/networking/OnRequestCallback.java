package distributed.systems.networking;

public interface OnRequestCallback {
    byte[] handleRequest(byte[] requestPayload);
    String getEndpoint();
}
