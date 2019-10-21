package wetterServicePackage;

//import hda.ds_lab.weather_service.thrift.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class WeatherServiceUser {
    private static long ai_user_id = 0;
    private long user_id;
    private long session_token;
    private Location location;

    public WeatherServiceUser(Location location, long session_token) {
        user_id = ++ai_user_id;
        this.location = location;
        this.session_token = session_token;
    }

    public long getUserID() { return user_id; }
    public long getSessionToken() { return session_token; }
    public Location getLocation() { return location; }
}

public class WeatherServiceImplementation implements Weather.Iface {
    private static String reports_filepath = "data/reports.json";
    private ArrayList<WeatherServiceUser> users = new ArrayList<>();

    public JSONArray loadReports() {
        JSONArray reports = new JSONArray();
        try {
            File f = new File(reports_filepath);
            if(f.exists() && !f.isDirectory()) {
                JSONParser json_parser = new JSONParser();
                try(FileReader reader = new FileReader(reports_filepath)){
                    Object file_contents = json_parser.parse(reader);
                    reports = (JSONArray) file_contents;
                    return reports;
                } catch(IOException e) {
                    System.out.println("FEHLER: Report-Datei konnte nicht gelesen werden "+reports_filepath+" ("+e.getMessage()+")");
                }
            } else {
                System.out.println("FEHLER: Report-Datei konnte nicht geöffnet werden "+reports_filepath+".");
            }
        } catch (Exception e) {
            System.out.println("FEHLER: Report-Datei konnte nicht interpretiert werden ("+e.getMessage()+").");
        }
        return reports;
    }
 
    @Override
    public long login(Location location) throws LocationException, org.apache.thrift.TException {
        for(int i = 0; i < users.size(); i++){
            if(users.get(i).getLocation().locationID == location.locationID){
                throw new LocationException(location, "FEHLER: Login ist fehlgeschlagen. Benutzer bereits vorhanden "+users.get(i).getUserID()+".");
            }
        }
        Random random = new Random();
        long sessionToken = random.nextLong();
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getSessionToken() == sessionToken) {
                sessionToken = random.nextLong();
                i = 0;
            }
        }
        WeatherServiceUser user =  new WeatherServiceUser(location, sessionToken);
        users.add(user);
        System.out.println("Eine WetterStation hat sich erfolgreich eingeloggt (Benuter-Id: "+user.getUserID()+").");
        return sessionToken;
    }
 
    @Override
    public boolean logout(long sessionToken) throws UnknownUserException, org.apache.thrift.TException {
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getSessionToken() == sessionToken) {
                System.out.println("Eine WetterStation hat sich erfolgreich ausgeloggt (Benuter-Id: "+users.get(i).getUserID()+").");
                users.remove(i);
                return true;
            }
        }
        throw new UnknownUserException(sessionToken, "FEHLER: Eine Wetterstation konnte sich nicht erfolgreich ausloggen, da die SessionToken unbekannt ist.");
    }

    // ankommende Daten
    @Override
    public boolean sendWeatherReport(WeatherReport report, long sessionToken) throws UnknownUserException, ReportException, DateException, org.apache.thrift.TException {
        WeatherServiceUser user = null;
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getSessionToken() == sessionToken) {
                user = users.get(i);
            }
        }
        if(user == null) throw new UnknownUserException(sessionToken, "FEHLER: WetterReport konnte aufgrund eines fehlenden SessionTokens nicht gesendet werden.");

        System.out.println("WetterStation mit der Id "+user.getUserID()+" hat einen WetterReport gesendet.");

        JSONArray reports = this.loadReports();
        JSONObject reports_entry = new JSONObject();
        if(report.report.getValue() <= 0 && report.report.getValue() > 4) {
            System.out.println("FEHLER: Falscher Wert für das Report-Feld: \""+report.report.getValue()+"\".");
            throw new ReportException(report.report, "Invalid value for the report field.");
        }
        reports_entry.put("report", report.report.getValue());
        JSONObject report_location = new JSONObject();
        report_location.put("location_id", report.location.locationID);
        report_location.put("name", report.location.name);
        report_location.put("latitude", report.location.latitude);
        report_location.put("longitude", report.location.longitude);
        report_location.put("description", report.location.description);
        reports_entry.put("location", report_location);
        reports_entry.put("temperature", report.temperature);
        reports_entry.put("humidity", report.humidity);
        reports_entry.put("wind_strength", report.windStrength);
        reports_entry.put("rainfall", report.rainfall);
        reports_entry.put("wind_direction", report.windDirection);
        reports_entry.put("datetime", report.dateTime);
        reports_entry.put("atmospheric_pressure", report.atmosphericpressure);
        reports.add(reports_entry);

        try {
            FileWriter file = new FileWriter(reports_filepath);
            file.write(reports.toJSONString());
            file.flush();
            file.close();
            System.out.println("Schreiben der empfangenen Daten "+reports_filepath+" ("+reports.size()+" insgesamt).");
            String env_var_ip1 = "env_var_ip1";
            // verbindung zu den anderen wetterservices
            try (TTransport transport = new TSocket("localhost", 9092)) {
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                Calc.Client client = new Calc.Client(protocol);
                System.out.println("Sende Daten:" + client.sendData("haloo"));
            } catch (TException x) {
                x.printStackTrace();
            }
            // verbindung zu den anderen wetterservices
            try (TTransport transport = new TSocket("localhost", 9092)) {
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                Calc.Client client = new Calc.Client(protocol);
                System.out.println("add result:" + client.sendData("haloo"));
            } catch (TException x) {
                x.printStackTrace();
            }
        } catch(IOException e) {
            System.out.println("FEHLER: Empfangene Daten konnten nicht geschrieben werden. Datei: "+reports_filepath+" ("+e.getMessage()+").");
        }

        return true;
    }

    @Override
    public WeatherReport receiveForecastFor(long userId, String time) throws UnknownUserException, DateException, TException {
        return null;
    }

    @Override
    public WeatherWarning checkWeatherWarnings(long userId) throws UnknownUserException, TException {
        return null;
    }

    @Override
    public boolean sendWarning(SystemWarning systemWarning, long userId) throws UnknownUserException, TException {
        return false;
    }
}