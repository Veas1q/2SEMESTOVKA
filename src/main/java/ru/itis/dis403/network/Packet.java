package ru.itis.dis403.network;

public class Packet {
    public PacketType type;
    public String data;

    public Packet(PacketType type, String data) {
        this.type = type;
        this.data = data;
    }
    public String serialize() {
        return type + "|" + data;
    }

    public static Packet deserialize(String s) {
        String[] p = s.split("\\|", 2);
        return new Packet(PacketType.valueOf(p[0]), p[1]);
    }
}
