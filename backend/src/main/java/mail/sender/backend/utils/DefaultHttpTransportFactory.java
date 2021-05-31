package mail.sender.backend.utils;

import com.google.api.client.http.HttpTransport;
import com.google.auth.http.HttpTransportFactory;

public class DefaultHttpTransportFactory implements HttpTransportFactory {
    private HttpTransport httpTransport;

    public DefaultHttpTransportFactory(HttpTransport httpTransport) {
        this.httpTransport = httpTransport;
    }

    @Override
    public HttpTransport create() {
        return httpTransport;
    }
}
