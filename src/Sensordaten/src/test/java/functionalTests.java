
import org.junit.jupiter.api.Test;
import sensordatenPackage.MqttPublisher;

import static org.junit.Assert.*;

public class functionalTests {
    @Test
    public void proofCorrectMessageConstruction(){
       assertEquals(9, MqttPublisher.getMessageWithRandomValues("1","luft").split(" | ").length);
    }
    @Test
    public void name2() {
    }


}