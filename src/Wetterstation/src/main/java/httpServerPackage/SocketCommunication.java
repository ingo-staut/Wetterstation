package httpServerPackage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketCommunication implements Runnable {

    private ServerSocket server;
    static final String fileName_internSensorData = "internSensorData.txt";


    public SocketCommunication(ServerSocket _server){
        server = _server;
    }

    @Override
    public void run() {
        while(true){
            System.out.println("Client kann Daten schicken...");
            // Socket erstellen und auf Client warten
            try {
                Socket socket = server.accept();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                // ObjectInputStream Objekt zu String umwandeln
                String message = (String) ois.readObject();
                System.out.println("\nNachricht empfangen: " + message);

                // In Text-Datei speichern
                try (FileWriter fileWriter = new FileWriter(fileName_internSensorData, true)) {
                    fileWriter.write( "\n" + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //close resources
                ois.close();
//                oos.close();
                socket.close();
                //Server beenden wenn die Nachricht: "exit" vom Client kommt
                if(message.equalsIgnoreCase("exit")) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }
}
