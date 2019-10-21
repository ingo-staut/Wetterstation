package httpServerPackage;

import java.net.*;
import java.io.IOException;
import java.util.Date;

public class Main {

    private static ServerSocket server;
//    private static int port = 9876;
    static final int PORT = 8080;
    static final boolean verbose = true;

    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        MqttSubscriber subscriber = new MqttSubscriber();
        subscriber.run();

//        server = new ServerSocket(port);
//        SocketCommunication c = new SocketCommunication(server);
//        Thread threadCommunication = new Thread(c);
//        threadCommunication.start();

        // HTTP-Server
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nActual port: " + PORT + "...\n");

            Thread wetter = new WeatherStationThriftClient(9091);
            wetter.start();

            while(true) {
                HTTPServer myServer = new HTTPServer(serverConnect.accept());

                if(verbose) {
                    System.out.println("Opened connection: " + new Date() + ")");
                }

                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }

        System.out.println("Socket Server is shuting of!");
//        server.close();
    }
}

