import httpServerPackage.Sensor;
import org.junit.jupiter.api.Test;
import httpServerPackage.HTTPServer;
import org.junit.Assert;

import org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class functionalTests {
    @Test
    public void proofContentType() {
           Assert.assertEquals("text/plain",HTTPServer.getContentType("returnJsonFile.json"));
           Assert.assertEquals("text/html",HTTPServer.getContentType("index.html"));
    }

    @Test
    public void proofCreateJSONObject() {
        Assert.assertEquals("{\"id\":1,\"type\":\"wind\",\"value\":75}", HTTPServer.createJSONObject(new Sensor(1, "wind", 1559589799102L, 75), "/sensors/1", false).toJSONString());
        Assert.assertEquals("{\"id\":1,\"type\":\"luft\",\"value\":60}", HTTPServer.createJSONObject(new Sensor(1, "luft", 1559589799102L, 60), "/sensors/1", false).toJSONString());
    }


}