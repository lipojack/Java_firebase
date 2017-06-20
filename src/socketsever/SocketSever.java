package socketsever;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONObject;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.google.firebase.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;

public class SocketSever {


	// Provide absolute path
	//private static String gazeFile = "W:\\GazeEstimationFor志軒\\IntraFace-v1.2\\x64\\Release\\output_gaze.json";
	//private static String gestureFile = "W:\\demo\\HandPoseRecognition-master\\HandPoseRecognition\\Release\\output_gesture.json";
	//private static String emotionFile = "";


	// Test file
	private static String gestureFile = "output_gesture.json";
	private static String gazeFile = "output_gaze.json";
	private static String emotionFile = "output_face.json";

	//static Boolean sent = false;

	static int[] buffer_gesture = new int[10];
	static int[] buffer_gaze = new int[10];
	static int[] buffer_emotion = new int[10];

	static int counter_gesture = 0;
	static int counter_gaze = 0;
	static int counter_emotion = 0;

	static int value_gesture;
	static int value_gaze;
	static int value_emotion;

	//static int value_gaze_pre = -1;


	public static void main(String[] args) {
		// Firebase initialize
		try {
			// [START initialize]
			FileInputStream serviceAccount = new FileInputStream("service-account.json");
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
					.setDatabaseUrl("https://nthusmarttv-d20c4.firebaseio.com/")
					.build();
			FirebaseApp.initializeApp(options);
			System.out.println("Firebase connect success");
			// [END initialize]
		} catch (IOException e) {
			System.out.println("ERROR: invalid service account credentials. See README.");
			System.out.println(e.getMessage());
			System.exit(1);
		}

		// Create database reference
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("User/103087087");
		DatabaseReference gestureRef = ref.child("Gesture").child("gesture");
		DatabaseReference gazeRef = ref.child("Gaze").child("gaze");
		DatabaseReference emotionRef = ref.child("Expression").child("expr");

		try {

			System.out.println("Start");

			//Filtering json data
			//Uploading json data to Firebase
			while (true) {
				// Gesture
				String filename = gestureFile;
				BufferedReader gesturebr = new BufferedReader(new FileReader(filename));
				StringBuilder gesturesb = new StringBuilder();
				String line = gesturebr.readLine();
				while (line == null) {
					line = gesturebr.readLine();
				}
				gesturesb.append(line);
				String string = gesturesb.toString();
				JSONObject gestureJson = new JSONObject(string);
				// filter: get mode
				buffer_gesture[counter_gesture % 10] = gestureJson.getInt("value");
				counter_gesture++;
				if (counter_gesture > 9) {
					value_gesture = mode(buffer_gesture);
					System.out.println("Gesture : " + value_gesture);
					switch(value_gesture){
						case 1:
							gestureRef.setValue("up");
							break;
						case 9:
							gestureRef.setValue("down");
							break;
						default:
							gestureRef.setValue("");
							break;
					}
					/*
					// determine sent
					if (value_gesture == 1 && sent == false) {
						// send
						gestureMap.clear();
						gestureMap.put("type", gestureJson.getString("type"));
						gestureMap.put("value", 1);
						String gestureJsonString = "";
						gestureJsonString = gestureJson.toString();
						byte[] gestureJsonByte = gestureJsonString.getBytes();
						System.out.println("Send gesture: " + gestureJsonString);
						out.write(gestureJsonByte);
						out.flush();
						sent = true;
					} else if (!(value_gesture == 1)) {
						sent = false;
					}
					*/
				}
				gesturebr.close();

				// Gaze
				filename = gazeFile;
				BufferedReader gazebr = new BufferedReader(new FileReader(filename));
				StringBuilder gazesb = new StringBuilder();
				line = gazebr.readLine();
				while (line == null) {
					line = gazebr.readLine();
				}
				gazesb.append(line);
				string = gazesb.toString();
				JSONObject gazeJson = new JSONObject(string);
				// filter: get mode
				buffer_gaze[counter_gesture % 10] = gazeJson.getInt("value");
				counter_gaze++;

				if (counter_gaze > 9) {
					value_gaze = mode(buffer_gaze);
					System.out.println("Gaze : " + value_gaze);
					// update firebase gaze value
					switch(value_gaze){
						case 0:
							gazeRef.setValue(true);
							break;
						case 1:
							gazeRef.setValue(false);
							break;
						default:
							break;
					}

					/*
					// send
					if (value_gaze != value_gaze_pre) {
						gazeMap.clear();
						gazeMap.put("type", gestureJson.getString("type"));
						gazeMap.put("value", value_gaze);
						String gazeJsonString = "";
						gazeJsonString = gazeJson.toString();
						byte[] gazeJsonByte = gazeJsonString.getBytes();
						System.out.println("Send gaze: " + gazeJsonString);
						out.write(gazeJsonByte);
						out.flush();
						gazebr.close();
						value_gaze_pre = value_gaze;
					}
					*/
				}
				gazebr.close();
				// Emotion
				// 0:'Anger'
				// 1:'Disgust'
				// 2:'Fear'
				// 3:'Happiness'
				// 4:'Sadness'
				// 5:'Surprise'
				// 6:'Neutral'
				filename = emotionFile;
				BufferedReader emotionbr = new BufferedReader(new FileReader(filename));
				StringBuilder emotionsb = new StringBuilder();
				line = emotionbr.readLine();
				while (line == null) {
					line = emotionbr.readLine();
				}
				emotionsb.append(line);
				string = emotionsb.toString();
				JSONObject emotionJson = new JSONObject(string);
				// filter: get mode
				buffer_emotion[counter_emotion % 10] = emotionJson.getInt("value");
				counter_emotion++;
				if (counter_emotion > 9) {
					value_emotion = mode(buffer_emotion);
					System.out.println("Emotion : " + value_emotion);
					switch(value_emotion){
						case 0:
							emotionRef.setValue("Anger");
							break;
						case 1:
							emotionRef.setValue("Disgust");
							break;
						case 2:
							emotionRef.setValue("Fear");
							break;
						case 3:
							emotionRef.setValue("Happiness");
							break;
						case 4:
							emotionRef.setValue("Sadness");
							break;
						case 5:
							emotionRef.setValue("Suprise");
							break;
						case 6:
							emotionRef.setValue("Neutral");
							break;
						default:
							System.out.println("Emotion exception");
							break;
					}

				}
				emotionbr.close();

				Thread.sleep(200);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// return the mode of the 10 numbers
	public static int mode(int[] array) {
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		int max = 1;
		int temp = 0;
		for (int i = 0; i < array.length; i++) {
			if (hm.get(array[i]) != null) {
				int count = hm.get(array[i]);
				count = count + 1;
				hm.put(array[i], count);
				if (count > max) {
					max = count;
					temp = array[i];
				}
			} else {
				hm.put(array[i], 1);
			}
		}
		return temp;
	}

}