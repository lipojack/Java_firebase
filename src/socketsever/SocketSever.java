package socketsever;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class SocketSever {

	private static Thread th_close;
	private static int serverport = 7777;
	private static ServerSocket serverSocket;
	private static ArrayList<Socket> socketlist = new ArrayList<Socket>();
	private static Map gazeMap = new HashMap();
	private static Map gestureMap = new HashMap();

	private static String gazeFile = "W:\\GazeEstimationFor志軒\\IntraFace-v1.2\\x64\\Release\\output_gaze.json";
	private static String gestureFile = "W:\\demo\\HandPoseRecognition-master\\HandPoseRecognition\\Release\\output_gesture.json";
	// private static String gestureFile = "gesture.json";
	// private static String gazeFile = "gaze.json";

	static Boolean sent = false;

	static int[] buffer_gesture = new int[10];
	static int[] buffer_gaze = new int[10];
	static int counter_gesture = 0;
	static int counter_gaze = 0;
	static int value_gesture;
	static int value_gaze;
	static int value_gaze_pre = -1;

	public static void main(String[] args) {

		// initial the array for mode calculator
		try {
			int i;
			for (i = 0; i < 10; i++) {
				buffer_gesture[i] = 0;
			}
			
			gestureMap.clear();
			// gestureFile = args[0];
			// gazeFile = args[1];

			System.out.println("Server Start");

			// set sever port
			serverSocket = new ServerSocket(serverport);
			th_close = new Thread(Judge_Close);
			th_close.start();
			while (!serverSocket.isClosed()) {
				waitNewSocket();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void sleep(int i) {

	}

	// detect disconnection
	private static Runnable Judge_Close = new Runnable() {
		@Override
		public void run() {
			System.out.println("Judge_Close run()");
			try {
				while (true) {
					// run every 2 sec
					Thread.sleep(2000);
					for (Socket close : socketlist) {
						if (isServerClose(close)) {
							// remove socket from socket list when disconnect
							socketlist.remove(close);
							System.out.println("socketlist.remove");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// detect disconnection
	private static Boolean isServerClose(Socket socket) {
		System.out.println("isServerClose");
		try {
			// if connect
			socket.sendUrgentData(0);
			return false;
		} catch (Exception e) {
			// if disconnect
			return true;
		}
	}

	// wait for client connection
	public static void waitNewSocket() {
		System.out.println("waitNewSocket");
		try {
			Socket socket = serverSocket.accept();
			createNewThread(socket);
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

	// create thread
	public static void createNewThread(final Socket socket) {
		System.out.println("createNewThread");
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socketlist.add(socket);

					BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

					while (socket.isConnected()) {
						
						/* Gesture */
						// read file
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
							System.out.println("value_gesture: " + value_gesture);
							
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
						}
						gesturebr.close();

						/* Gaze */
						// read file
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
							System.out.println("value_gaze: " + value_gaze);

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
						}

						Thread.sleep(200);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
}