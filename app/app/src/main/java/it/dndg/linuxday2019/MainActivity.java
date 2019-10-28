package it.dndg.linuxday2019;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "LinuxDay2019";
    private final static String URL = "http://localhost/index.html";

    private FrameLayout frame;
    private WebView webview;
    private WebMessagePort port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        frame = findViewById(R.id.frame);

        WebView.setWebContentsDebuggingEnabled(true);

        WebViewProxy proxy = new WebViewProxy(this);

        webview = findViewById(R.id.webview);
        webview.setWebViewClient(proxy);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);

        webview.loadUrl(URL);
    }

    public void setupPorts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final WebMessagePort[] channel = webview.createWebMessageChannel();
            port = channel[0];
            port.setWebMessageCallback(new WebMessagePort.WebMessageCallback() {
                @Override
                public void onMessage(WebMessagePort port, WebMessage message) {
                    processWebMessage(message);
                }
            });

            webview.postWebMessage(
                    new WebMessage("", new WebMessagePort[]{channel[1]}),
                    Uri.parse(URL)
            );
        }
        else {
            Log.e(TAG, "Not supported on API < 23");
        }
    }

    private void processWebMessage(WebMessage message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String command;
            String payload;

            String data = message.getData();
            String[] parts;

            int payloadIndex = data.indexOf(' ');
            if (payloadIndex > 0) {
                command = data.substring(0, payloadIndex);
                payload = data.substring(payloadIndex + 1);
            }
            else {
                command = data;
                payload = null;
            }

            Log.d(TAG, "Received command: " + command);

            processCommand(command, payload);
        }
        else {
            Log.e(TAG, "Not supported on API < 23");
        }
    }

    private void sendWebMessage(String data) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            port.postMessage(new WebMessage(data));
        }
        else {
            Log.e(TAG, "Not supported on API < 23");
        }
    }

    private void processCommand(String command, String payload) {
        String[] parts;

        switch (command) {
            case "PING":
                sendWebMessage("PONG");
                break;

            case "CLEAR":
                commandClear();
                break;

            case "LINEAR-LAYOUT":
                parts = payload.split("/");
                if (parts.length >= 1) {
                    int orientation = parts[0].equals("HORIZONTAL") ?
                            LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;
                    commandAddLinearLayload(parts.length == 2 ? parts[1] : null, orientation);
                }
                else {
                    sendWebMessage("ERROR: Missing parameters");
                }
                break;

            case "TEXT":
                parts = payload.split("/");
                if (parts.length >= 1) {
                    commandAddText(parts.length == 2 ? parts[1] : null, parts[0]);
                }
                else {
                    sendWebMessage("ERROR: Missing parameters");
                }
                break;

            case "TEXT-UPDATE":
                parts = payload.split("/");
                if (parts.length == 2) {
                    commandUpdateText(parts[0], parts[1]);
                }
                else {
                    sendWebMessage("ERROR: Missing parameters");
                }
                break;

            case "BUTTON":
                parts = payload.split("/");
                if (parts.length >= 1) {
                    commandAddButton(parts.length == 2 ? parts[1] : null, parts[0]);
                }
                else {
                    sendWebMessage("ERROR: Missing parameters");
                }
                break;


            default:
                sendWebMessage("ERROR: Unknown command: " + command);
                break;
        }
    }


    private ViewGroup currentViewGroup;

    private void commandClear() {
        frame.removeAllViews();

        idMap.clear();

        currentViewGroup = frame;
    }

    private void commandAddLinearLayload(final String id, int orientation) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 16,16, 16);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(params);
        ll.setOrientation(orientation);

        currentViewGroup.addView(ll, params);
        currentViewGroup = ll;

        registerId(ll, id);
    }

    private void commandAddText(final String id, String text) {
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(20);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        currentViewGroup.addView(tv, params);

        registerId(tv, id);
    }

    private void commandAddButton(final String id, String text) {
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(20);
        b.setGravity(Gravity.CENTER_HORIZONTAL);

        b.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View v) {
                sendWebMessage("CLICK " + id);
            }
        });

        currentViewGroup.addView(b, params);

        registerId(b, id);
    }

    private void commandUpdateText(final String id, String text) {
        if (idMap.containsKey(id)) {
            Integer androidId = idMap.get(id);
            TextView tv = findViewById(androidId.intValue());
            tv.setText(text);
        }
    }

    private Map<String,Integer> idMap = new HashMap<>();
    private Integer idCounter = new Integer(0);

    private void registerId(View view, String id) {
        if (id != null) {
            idCounter += 1;
            idMap.put(id, idCounter);
            view.setId(idCounter.intValue());
        }
    }

}
