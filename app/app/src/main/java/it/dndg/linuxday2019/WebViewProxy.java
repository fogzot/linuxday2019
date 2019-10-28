package it.dndg.linuxday2019;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class WebViewProxy extends WebViewClient {
    private final static String TAG = "LinuxDay2019";

    private final MainActivity activity;

    WebViewProxy(MainActivity activity) {
        super();

        this.activity = activity;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request) {
        WebResourceResponse response = null;

        String requestHost = request.getUrl().getHost();
        String requestPath = request.getUrl().getPath();

        Log.d(TAG,"Received request: host = " + requestHost + ", path = " + requestPath);

        if (requestHost.equals("localhost")) {
            Map<String,String> headers = null;

            // If more than one domain is intercepted we may need CORS headers.
            // Map<String,String> headers = new HashMap<>();
            // headers.put("Access-Control-Allow-Origin", "*");

            String assetsPath = requestPath.substring(1);
            try {
                InputStream stream = activity.getAssets().open(assetsPath);

                String encoding = "UTF-8";
                String mimeType;

                String extension = assetsPath.substring(assetsPath.lastIndexOf('.') + 1);

                switch (extension) {
                    case "js":
                        mimeType = "application/javascript";
                        break;
                    case "html":
                        mimeType = "text/html";
                        break;
                    case "css":
                        mimeType = "text/css";
                        break;
                    default:
                        // Set encoding to null, because we don't know it.
                        mimeType = "application/octet-stream";
                        encoding = null;
                        break;
                }

                response = new WebResourceResponse(
                        mimeType,
                        encoding,
                        200,
                        "OK",
                        headers,
                        stream
                );
            }
            catch (IOException ex) {
                InputStream stream = new ByteArrayInputStream(
                        "The file was there, but we hid it.".getBytes(Charset.forName("UTF-8")));

                response = new WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        404,
                        "Not found",
                        headers,
                        stream
                );
            }
        }

        // If response is null, this lets Android proceed with default WebView URL loading.
        return response;
    }

    @Override
    public void onPageFinished (WebView view, String url) {
        activity.setupPorts();
    }
}
