/*
 Copyright (c) 2017, Michael Bredel, H-DA
 ALL RIGHTS RESERVED.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 Neither the name of the H-DA and Michael Bredel
 nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written
 permission.
 */

package httpServerPackage;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FileWriter;
import java.io.IOException;

public class SimpleMqttCallback implements MqttCallback {

    /** The logger. */
//    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMqttCallback.class);
    static final String fileName_internSensorData = "internSensorData.txt";

    @Override
    public void connectionLost(Throwable throwable) {
//        LOGGER.error("Connection to MQTT broker lost!");
        System.out.println("Connection to MQTT broker lost!");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//        LOGGER.info("Message received: "+ new String(mqttMessage.getPayload()) );
        System.out.println("Message received: "+ new String(mqttMessage.getPayload()));

        // In Text-Datei speichern
        try (FileWriter fileWriter = new FileWriter(fileName_internSensorData, true)) {
            String message = new String(mqttMessage.getPayload());
            fileWriter.write(System.lineSeparator() + message);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Datei konnte nicht geschrieben werden.");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken mqttDeliveryToken) {
        //            LOGGER.info("Delivery completed: "+ mqttDeliveryToken.getMessage() );
        try {
            System.out.println("Delivery completed: "+ mqttDeliveryToken.getMessage());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
