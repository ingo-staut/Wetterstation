package sensordatenPackage;

public class MqttParam {

    private static MqttParam mqttParamInstance;
    private String brokerAddress = "iot.eclipse.org";
    private String brokerPort = "1883";
    private String brokerProtocol = "tcp";
    private String mqttTopic = "topic_weather";

    public static MqttParam getMqttParamInstance() {
        if (mqttParamInstance == null)
            mqttParamInstance = new MqttParam();
        return mqttParamInstance;
    }

    public String getBrokerAddress() {
        return this.brokerAddress;
    }

    public String getBrokerPort() {
        return this.brokerPort;
    }

    public String getBrokerProtocol() {
        return this.brokerProtocol;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    private MqttParam() {}
}