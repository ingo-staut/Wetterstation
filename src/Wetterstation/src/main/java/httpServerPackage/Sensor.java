package httpServerPackage;

import java.util.TreeMap;

public class Sensor {
    private String type;
    private int id;
    private TreeMap<Long, Integer> values;

    public Sensor(int id, String type, long timestamp, int value) {
        this.id = id;
        this.type = type;
        values = new TreeMap<>();
        this.values.put(timestamp, value);
    }

    public Sensor(int id, String type, TreeMap<Long, Integer> values) {
        this.id = id;
        this.type = type;
        this.values = values;
    }

    public int getID() {
        return id;
    }

    public int getLastValue() {
        return values.lastEntry().getValue();
    }

    public TreeMap<Long, Integer> getValues() {
        return values;
    }

    public String getType() {
        return type;
    }

    public void addValue(long timestamp, int value) {
        values.put(timestamp, value);
    }
}