package httpServerPackage;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttSubscriber {

    private MqttParam mqttParam;
    private String broker;

    public MqttSubscriber() {
        mqttParam = MqttParam.getMqttParamInstance();
        broker = mqttParam.getBrokerProtocol() + "://" +  mqttParam.getBrokerAddress() + ":" + mqttParam.getBrokerPort();

    }

    public void run() {
        try {
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
            client.setCallback(new SimpleMqttCallback());

            client.connect();
            System.out.println("Connected to MQTT broker: " + client.getServerURI());

            client.subscribe(mqttParam.getMqttTopic());
            System.out.println("Subscribed to topic: " + client.getTopic(mqttParam.getMqttTopic()));
        } catch (MqttException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}