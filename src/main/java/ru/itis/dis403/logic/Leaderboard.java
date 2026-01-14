package ru.itis.dis403.logic;

import java.io.*;
import java.util.*;

public class Leaderboard {

    private static final String FILE = "leaderboard.txt";
    private static final Map<String, Integer> scores = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        File f = new File(FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("=");
                if (p.length == 2) {
                    scores.put(p[0], Integer.parseInt(p[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (var e : scores.entrySet()) {
                pw.println(e.getKey() + "=" + e.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addKills(String name, int kills) {
        if (name == null || name.isBlank()) return;

        scores.put(name, scores.getOrDefault(name, 0) + kills);
        save();
    }

    public static List<Map.Entry<String, Integer>> top5() {
        return scores.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .toList();
    }
}
