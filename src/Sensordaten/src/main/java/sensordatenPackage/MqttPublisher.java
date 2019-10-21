package sensordatenPackage;

import org.eclipse.paho.client.mqttv3.*;
import java.util.Date;
import java.util.Random;

public class MqttPublisher {

    private MqttParam mqttParam;
    private String broker;
    private String sensorId;
    private String sensorTyp;
    private static String delimiter = " | ";

    public MqttPublisher(String sensorId, String sensorTyp) {
        mqttParam = MqttParam.getMqttParamInstance();
        broker = mqttParam.getBrokerProtocol() + "://" +  mqttParam.getBrokerAddress() + ":" + mqttParam.getBrokerPort();

        this.sensorId = sensorId;
        this.sensorTyp = sensorTyp;
    }

    public void run() {
        MqttConnectOptions mqttConnectOpts = new MqttConnectOptions();
        mqttConnectOpts.setCleanSession(true);

        try {

            MqttClient client = new MqttClient(broker, MqttClient.generateClientId());

            client.connect(mqttConnectOpts);
            System.out.println("Connected to MQTT broker: " + client.getServerURI());

            MqttMessage message = new MqttMessage(getMessageWithRandomValues(sensorId, sensorTyp).getBytes());
            message.setQos(2);

            client.publish(mqttParam.getMqttTopic(), message);
            System.out.println("Published message: " + message);

            client.disconnect();
            System.out.println("Disconnected from broker.");

        } catch (MqttException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static String getMessageWithRandomValues(String sensorId, String sensorTyp) {
        Random randomValues = new Random();
        String message = sensorId + delimiter + sensorTyp + delimiter;

        switch (sensorTyp) {
            case "luft":
                int luftfeuchtigkeit = randomValues.nextInt(101); // in %
                message += Integer.toString(luftfeuchtigkeit);
                break;
            case "temp":
                int temperatur = randomValues.nextInt(50); // in degree
                message += Integer.toString(temperatur);
                break;
            case "wind":
                int wind = randomValues.nextInt(200); // in kmh
                message += Integer.toString(wind);
                break;
            case "regen":
                int regen = randomValues.nextInt(100); // in millimeter
                message += Integer.toString(regen);
                break;
            default:
                throw new IllegalStateException("Unexpected Value: " + sensorTyp);
        }

        // timestamp
        Date date = new Date();
        long time = date.getTime();

        message = 0 + delimiter + message + delimiter + time;
        return message;
    }
}