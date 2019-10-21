package httpServerPackage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class HTTPServer implements Runnable {
    static final File root_directory = new File(".");
    static final String html_index = "index.html";
    static final String html_404 = "404.html";
    static final String fileName_returnJsonFile = "returnJsonFile.json";
    static final String fileName_internSensorData = "internSensorData.txt";
    static final String uri_sensors = "sensors";
    static final String uri_history = "history";

    static final int PORT = 8080;
    static final boolean logs = true;
    private Socket connect;

    private Map<Integer, Sensor> sensorMap = new HashMap<>();

    public HTTPServer(Socket c) {
        connect = c;
    }

    @Override
    public void run() {
        // managing particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            //Input: Daten vom Client
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            //Output: Daten für Header
            out = new PrintWriter(connect.getOutputStream());
            //Output: Angefragte Datei für den Client
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            //Erste Zeile der Client-Anfrage
            //In der zweiten Zeile befindet sich der Host
            //In der dritten Zeile befindet sich die Verbindung
            String input = in.readLine();
            String input_2 = in.readLine();
            String input_3 = in.readLine();
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); //HTTP Methode des Clients
            fileRequested = parse.nextToken().toLowerCase();

            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (logs) {
                    System.out.println("501 Nicht implementierte Methode :" + method);
                }
                fileNotFound(out, dataOut, fileRequested);

            } else {
                //Methode ist entweder GET oder HEAD
                if (!fileRequested.equals("/favicon.ico")) {

                    String fileName = "";
                    File file;

                    if (fileRequested.endsWith("/")) {
                        fileRequested += html_index;
                        fileName = html_index;
                        file = new File(root_directory, fileName);
                    } else {
                        fileName = fileName_returnJsonFile;
                        file = new File(root_directory, fileName);

                        // internSensorData einlesen
                        readTextFile();

                        // JSON-Datei erstellen

                        // mit id eines Sensors => ein Sensor
                        int pos_2 = fileRequested.indexOf("/" + uri_sensors + "/");

                        if (pos_2 != -1) {
                            String id_str = fileRequested.substring(pos_2 + 9);
                            int id = Integer.parseInt(id_str);

                            Sensor searchedSensor = sensorMap.get(id);
                            if (searchedSensor == null) {
                                fileNotFound(out, dataOut, fileRequested);
                            } else {
                                try (FileWriter jsonWriter = new FileWriter(file)) {
                                    jsonWriter.write(createJSONObject(searchedSensor, fileRequested, false).toJSONString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            // ohne id eines Sensors => Liste der Sensoren
                            int pos_3 = fileRequested.indexOf("/" + uri_sensors);

                            if (pos_3 != -1) {
                                try (FileWriter jsonWriter = new FileWriter(file)) {
                                    jsonWriter.write("{\"" + uri_sensors + "\": [");
                                    for (int i = 1; i <= sensorMap.size(); i++) {
                                        Sensor searchedSensor = sensorMap.get(i);
                                        if(searchedSensor != null) {
                                            jsonWriter.write(createJSONObject(searchedSensor, fileRequested, true).toJSONString() + ",");
                                        }
                                    }
                                    jsonWriter.write("]}");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                fileNotFound(out, dataOut, fileRequested);
                            }
                        }
                    }

                    int fileLength = (int) file.length();
                    String content = getContentType(fileName);

                    if (method.equals("GET")) { //GET Methode

                        byte[] fileData = readFileData(file, fileLength);

                        // HTTP Header
                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: Java HTTP Server for Weatherstation: 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + content);
                        out.println("Content-length: " + fileLength);
                        out.println();
                        out.flush(); //out leeren

                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
                    }

                    if (logs) {
                        System.out.println("Datei " + fileRequested + " vom Typ " + content + " zurückgegeben");
                    }
                }
            }

        } catch (
                FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Fehler in der Datei: " + ioe.getMessage());
            }

        } catch (
                IOException ioe) {
            System.err.println("Server Error: " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        if (logs) {
            System.out.println("Verbindung wurde geschlossen.\n");
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIN = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIN = new FileInputStream(file);
            fileIN.read(fileData);
        } finally {
            if (fileIN != null) {
                fileIN.close();
            }

        }
        return fileData;
    }

    static public String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(root_directory, html_404);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server for Weatherstation: 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (logs) {
            System.out.println("Datei " + fileRequested + " nicht gefunden");
        }
    }

    public void readTextFile() {
        try (FileReader reader = new FileReader(fileName_internSensorData)) {
            List<String> list = Files.readAllLines(Paths.get(fileName_internSensorData));
            for (String var : list
            ) {
                if (!var.isEmpty()) {
                    String[] parts = var.split(" \\| ");
                    int messageId = Integer.parseInt(parts[0]);
                    int sensorId = Integer.parseInt(parts[1]);
                    String sensorTyp = parts[2];
                    int sensorValue = Integer.parseInt(parts[3]);
                    long timestamp = Long.parseLong(parts[4]);
                    Sensor sensor = sensorMap.get(sensorId);
                    if (sensor != null) {
                        sensor.addValue(timestamp, sensorValue);
                    } else {
                        sensor = new Sensor(sensorId, sensorTyp, timestamp, sensorValue);
                        sensorMap.put(sensorId, sensor);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public JSONObject createJSONObject(Sensor searchedSensor, String fileRequested, Boolean linkIsNeeded) {
        String link = "";
        JSONObject data = new JSONObject();
        data.put("id", searchedSensor.getID());
        data.put("type", searchedSensor.getType());
        // historisch
        if (fileRequested.contains("/" + uri_history + "/")) {
            JSONArray list = new JSONArray();
            for (Map.Entry entry : searchedSensor.getValues().entrySet()) {
//                                                list.add(entry.getKey());
                list.add(entry.getValue());
            }
            data.put("values", list);
            if(linkIsNeeded) {
                link = "/" + uri_history + "/" + uri_sensors + "/" + searchedSensor.getID();
            }
        } else {
            data.put("value", searchedSensor.getLastValue());
            if(linkIsNeeded){
                link = "/" + uri_sensors + "/" + searchedSensor.getID();
            }
        }
        if(linkIsNeeded){
            data.put("link", link);
        }
        return data;
    }
}
