package completetravelplannerserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import completetravelplannerserver.org.json.JSONArray;
import completetravelplannerserver.org.json.JSONException;
import completetravelplannerserver.org.json.JSONObject;

public class GCMSender {
	private static final String serverURL = "https://android.googleapis.com/gcm/send";
	private static final String authKey = "AIzaSyB2Fwei8Y2pKUQpUmdZ49W86T833Bv4jpE";
	private static final Logger log = Logger.getLogger(GCMSender.class
			.getName());

	public static void sendGCMNotification(String[] regIds, String message) {
		try {
			JSONArray array = new JSONArray();
			for (String id : regIds) {
				array.put(id);
			}
			StringBuilder response = new StringBuilder();
			JSONObject msg = new JSONObject();
			msg.put("registration_ids", array);
			msg.put("data", new JSONObject().put("message", message));
			log.info("GCM Message: " + msg.toString());
			URL url = new URL(serverURL);
			byte[] bytes = msg.toString().getBytes();
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Authorization", "key=" + authKey);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode(bytes.length);
			OutputStream out = urlConnection.getOutputStream();
			out.write(bytes);
			out.close();
			int result = urlConnection.getResponseCode();
			log.info("Received response code: " + result);
			InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
			int charCode = -1;
			while ((charCode = in.read()) != -1) {
				response.append((char) charCode);
			}
			in.close();
			log.info("received GCM response " + response.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
