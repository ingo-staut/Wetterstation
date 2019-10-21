package httpServerPackage;

//import hda.ds_lab.weather_station.thrift.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.sun.org.apache.xpath.internal.operations.Bool;

class WeatherStationThriftClient extends Thread implements Runnable {
    private int thrift_port = 9091;
    private Weather.Client thrift_client;
    private static Timer report_task_timer = new Timer();
    private Location location;
    private long session_token;

    private static String env_var_ip = "env_var_ip";

    static final String fileName_returnJsonFile = "returnJsonFile.json";
    static final String fileName_internSensorData = "internSensorData.txt";


    public WeatherStationThriftClient(int port) {
        this.thrift_port = port;
        this.location = new Location();
        this.location.locationID = (System.getenv("LOCATION_ID") != null) ? (byte) Integer.parseInt(System.getenv("LOCATION_ID")) : 1;
        try {
            this.location.name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.location.name = "Weather Station";
        }
        Random generator = new Random();
        this.location.latitude = -90.00 + (180.00) * generator.nextDouble(); // Random latitude between -90 (South Pole) and +90 (North Pole)
        this.location.latitude = -180.00 + (360.00) * generator.nextDouble(); // Random latitude between -180 (Far West) and +180 (Far East)
    }

    static class WeatherStationThriftReportTask extends TimerTask {
        private Weather.Client thrift_client;
        private Location location;
        private long session_token;

        private Map<Integer, Sensor> sensorMap = new HashMap<>();


        public WeatherStationThriftReportTask(Weather.Client thrift_client, long session_token, Location location) {
            this.thrift_client = thrift_client;
            this.session_token = session_token;
            this.location = location;
        }

        public JSONArray loadHistoryFile(String filepath) {
            JSONArray history = new JSONArray();
            try {
                File f = new File(filepath);
                if (f.exists() && !f.isDirectory()) {
                    JSONParser json_parser = new JSONParser();
                    try (FileReader reader = new FileReader(filepath)) {
                        Object file_contents = json_parser.parse(reader);
                        history = (JSONArray) file_contents;
                        return history;
                    } catch (IOException e) {
                        System.out.println("FEHLER: Folgende Datei wurde nicht gefunden: " + filepath + " (" + e.getMessage() + ")");
                    }
                } else {
                    System.out.println("FEHLER: Folgende Datei wurde nicht gefunden: " + filepath + ".");
                }
            } catch (Exception e) {
                System.out.println("FEHLER: Folgende Datei konnte nicht interpretiert werden: (" + e.getMessage() + ").");
            }
            return history;
        }

        @Override
        public void run() {
            WeatherReport report = new WeatherReport();

            readTextFile();


            //Sensor windSensor = null;
            //Sensor regenSensor = null;
            //Sensor luftSensor = null;
            //Sensor tempSensor = null;
            Boolean wind = true;
            Boolean regen = true;
            Boolean luft = true;
            Boolean temp = true;
            double humidity_double = 0;
            double wind_speed_double = 0;
            double last_temperature_value = 0;
            double last_rain_value = 0;
//            for (int i = sensorMap.size() - 1; i >= 0; i--) { // von hinten nach vorne
            for (int i = 1; i <= sensorMap.size(); i++) { // von hinten nach vorne
                Sensor searchedSensor = sensorMap.get(i);
                if (searchedSensor != null) {

                    if (searchedSensor.getType().equals("wind")
                            && wind) {
                        wind_speed_double = searchedSensor.getValues().lastEntry().getValue();
                        wind = false;
                    } else if (searchedSensor.getType().equals("regen")
                            && regen) {
                        last_rain_value = searchedSensor.getValues().lastEntry().getValue();
                        regen = false;
                    } else if (searchedSensor.getType().equals("luft")
                            && luft) {
                        humidity_double = searchedSensor.getValues().lastEntry().getValue();
                        luft = false;
                    } else if (searchedSensor.getType().equals("temp")
                            && temp) {
                        last_temperature_value = searchedSensor.getValues().lastEntry().getValue();
                        temp = false;
                    }
                    if (!wind && !regen && !luft && !temp) {
                        break;
                    }

                }
            }

            byte humidity_report = (byte) humidity_double;
            byte wind_speed_report = (byte) wind_speed_double;

            report.location = this.location;
            report.temperature = last_temperature_value;
            report.rainfall = last_rain_value;
            report.humidity = humidity_report;
            report.windStrength = wind_speed_report;
            report.atmosphericpressure = 0;
            report.windDirection = 0;
            if (report.temperature >= 0.0) {
                if (report.rainfall > 0.0) {
                    report.report = Report.RAINY;
                } else {
                    report.report = Report.SUNNY;
                }
            } else {
                if (report.rainfall > 0.0) {
                    report.report = Report.SNOW;
                } else {
                    report.report = Report.CLOUDY;
                }
            }
            Date date = new Date();
            SimpleDateFormat report_date_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            report_date_format.setTimeZone(TimeZone.getTimeZone("CET"));
            report.dateTime = report_date_format.format(date);


            try {
                this.thrift_client.sendWeatherReport(report, this.session_token);
                System.out.println("WetterReport wurde an den WetterService versendet.");
            } catch (UnknownUserException e) {
                System.out.println("1FEHLER: WetterReport konnte nicht an den WetterService geschickt werden: ( " + e.why + ").");
            } catch (ReportException e) {
                System.out.println("2FEHLER: WetterReport konnte nicht an den WetterService geschickt werden: (" + e.why + ").");
            } catch (DateException e) {
                System.out.println("3FEHLER: WetterReport konnte nicht an den WetterService geschickt werden: (" + e.why + ").");
            } catch (TException e) {
                System.out.println("4FEHLER: WetterReport konnte nicht an den WetterService geschickt werden: (" + e.getMessage() + ").");

            } catch (Exception e) {
                System.out.println("5FEHLER: WetterReport konnte nicht an den WetterService geschickt werden: (" + e.getMessage() + ").");
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
    }

    static public void connect(){

    }

    @Override
    public void run() {
        try {
            // sensorIp
            InetAddress sensorIp;
            if (System.getenv(env_var_ip) != null) {
                String ipAdress = System.getenv(env_var_ip); // mit Docker / Umgebungsvariable
                ipAdress = ipAdress.replace("\"", "");
                sensorIp = InetAddress.getByName(ipAdress);
            } else {
                sensorIp = InetAddress.getLocalHost(); // für Testzwecke ohne Docker
            }

            TTransport transport = new TSocket(sensorIp.getHostName(), this.thrift_port);
            transport.open();
            System.out.println("WetterStation Client wurde an Port " + this.thrift_port + " gestartet.");
            TProtocol protocol = new TBinaryProtocol(transport);
            this.thrift_client = new Weather.Client(protocol);
            this.session_token = thrift_client.login(this.location);

            report_task_timer.schedule(new WeatherStationThriftReportTask(this.thrift_client, this.session_token, this.location), 10000, 20000);
        } catch (Exception e) {
            System.out.println("FEHLER: WetterStation Client konnte nicht gestartet werden (" + e.getMessage() + ").");
        }
    }

}