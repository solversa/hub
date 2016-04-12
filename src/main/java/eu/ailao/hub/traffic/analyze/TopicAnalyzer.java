package eu.ailao.hub.traffic.analyze;

import eu.ailao.hub.traffic.analyze.dataclases.LoadedReferenceQuestions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;

/**
 * Created by Petr Marek on 4/11/2016.
 */
public class TopicAnalyzer {

	public TrafficTopic analyzeTrafficTopic(String question) {
		try {
			JSONObject jsonObject = askDatasetSTS(question);
			JSONArray probabilities = jsonObject.getJSONArray("score");
			double[] normalizedProbabilities = normalizeProbabilities(probabilities);
			TrafficTopic topic = determineTopicMax(normalizedProbabilities);
			return topic;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return TrafficTopic.UNKNOWN;
	}

	private JSONObject askDatasetSTS(String question) throws IOException {
		String response = "";
		URL url = new URL("http://pichl.ailao.eu:5050/score");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		String input = createQuerry(question);
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes());
		os.flush();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "UTF8"));

		String output;
		while ((output = br.readLine()) != null) {
			response += output;
		}
		conn.disconnect();
		return new JSONObject(response);
	}

	private String createQuerry(String question) {
		JSONObject querry = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		LoadedReferenceQuestions loadedReferenceQuestions = LoadedReferenceQuestions.getInstance();
		for (int i = 0; i < loadedReferenceQuestions.size(); i++) {
			jsonArray.put(stripAccents(loadedReferenceQuestions.getQuestion(i).toLowerCase().replace("?","")));
		}
		querry.put("qtext", question);
		querry.put("atext", jsonArray);
		return querry.toString();
	}

	private static String stripAccents(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}

	private TrafficTopic determineTopicMax(double[] probabilities) {
		TrafficTopic mostProbableTopic = null;
		double biggestProbability = -1;
		LoadedReferenceQuestions loadedReferenceQuestions = LoadedReferenceQuestions.getInstance();
		for (int i = 0; i < probabilities.length; i++){
			if (probabilities[i] > biggestProbability) {
				biggestProbability = probabilities[i];
				mostProbableTopic = loadedReferenceQuestions.getTrafficTopic(i);
			}
		}
		return mostProbableTopic;
	}

	private double[] normalizeProbabilities(JSONArray probabilities){
		double[] normalizedProbabilities = new double[probabilities.length()];
		for (int i = 0; i < probabilities.length(); i++) {
			normalizedProbabilities[i] = sigmoid((Double) probabilities.get(i));
		}
		return normalizedProbabilities;
	}

	public static double sigmoid(double x) {
		return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
}
