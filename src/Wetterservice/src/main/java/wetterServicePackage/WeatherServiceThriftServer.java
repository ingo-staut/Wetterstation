package wetterServicePackage;

//import hda.ds_lab.weather_service.thrift.*;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

class WeatherServiceThriftServer implements Runnable {
    private int thrift_port = 80;

    public WeatherServiceThriftServer(int port) {
        this.thrift_port = port;
    }

    @Override
    public void run() {
        try {
            TServerTransport serverTransport = new TServerSocket(this.thrift_port);
            TSimpleServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(new Weather.Processor<>(new WeatherServiceImplementation())));
            System.out.println("WetterService wurde mit dem Port " + this.thrift_port + " gestartet.");
            server.serve();
        } catch (TTransportException e) {
            System.out.println("FEHLER: Der WetterService konnte nicht gestartet werden. (" + e.getMessage() + ").");
        }
    }
}