package sensordatenPackage;

import java.io.*;
import java.net.*;

public class Main {

    private static int port = 9876;
    private static String delimiter = " | ";
    private static String env_var_type = "env_var_type";
    private static String env_var_id = "env_var_id";
    private static String env_var_ip = "env_var_ip";

    public static void main(String[] args) throws IOException, InterruptedException {

        // sensorType
        String sensorType;
        if (System.getenv(env_var_type) != null) {
            sensorType = System.getenv(env_var_type); // with docker / environment variable
            sensorType = sensorType.replace("\"", "");
        } else {
            sensorType = "wind"; // for testing without docker
        }

        // sensorId
        String sensorId;
        if (System.getenv(env_var_id) != null) {
            sensorId = System.getenv(env_var_id); // with docker / environment variable
            sensorId = sensorId.replace("\"", "");
        } else {
            sensorId = "1"; // for testing without docker
        }

        // sensorIp
        InetAddress sensorIp;
        if (System.getenv(env_var_id) != null) {
            String ipAdress = System.getenv(env_var_ip); // with docker / environment variable
            ipAdress = ipAdress.replace("\"", "");
            sensorIp = InetAddress.getByName(ipAdress);
        } else {
            sensorIp = InetAddress.getLocalHost(); // for testing without docker
        }

        ObjectOutputStream oos = null;
        Socket socket = null;

        // message structure
        // 1. messageId (incrementing)
        // 2. SensorId
        // 3. SensorType
        // 4. SensorValue

        while(true) {
            // Start the MQTT subscriber
            MqttPublisher publisher = new MqttPublisher(sensorId, sensorType);
            publisher.run();

            boolean trying = true;
            int counter = 0;
            while (trying) {
                try {
                    counter++;
                    socket = new Socket(sensorIp.getHostName(), port);
                    trying = false;
                } catch (Exception e) {
                    if (counter == 1) {
                        System.out.print("Start wetterstation! Sensor is waiting!");
                    }
                    System.out.print("."); // waiting dots
                    Thread.sleep(2000);
                }
            }

            oos = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("\nSend message to socket server.");

            oos.writeObject(MqttPublisher.getMessageWithRandomValues(sensorId, sensorType));
            oos.close();
            Thread.sleep(10000);
        }
//        socket.close(); // is never reached with a "while(true)"
    }
}


