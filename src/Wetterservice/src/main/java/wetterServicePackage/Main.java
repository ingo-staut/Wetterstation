package wetterServicePackage;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public  class Main {
    public static void main(String[] args) {

        WeatherServiceThriftServer wetter = new WeatherServiceThriftServer(9091);
        Thread thread = new Thread(wetter);
        thread.start();

        // connection to other weatherservices
        // handler for data
        startSimpleServer(new Calc.Processor<>(new ServerHandler()));
    }

    public static void startSimpleServer(Calc.Processor<ServerHandler> processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9092);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
            System.out.println("Starting the server ...");
            server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

