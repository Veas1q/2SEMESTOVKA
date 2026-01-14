package ru.itis.dis403.network;

import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private PrintWriter out;

    public ClientConnection(String ip, int port, PacketListener listener) {
        try {
            Socket socket = new Socket(ip, port);

            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()),
                    true
            );

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        Packet packet = Packet.deserialize(line);
                        listener.onPacket(packet);
                    }
                } catch (IOException e) {
                    System.out.println("Соединение разорвано");
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Не удалось подключиться к серверу");
        }
    }

    public void send(Packet packet) {
        out.println(packet.serialize());
    }

    public interface PacketListener {
        void onPacket(Packet packet);
    }
}
