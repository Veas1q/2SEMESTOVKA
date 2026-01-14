package ru.itis.dis403.logic;

import ru.itis.dis403.model.Bullet;
import ru.itis.dis403.model.GameMap;
import ru.itis.dis403.model.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    public Map<Integer, Player> players = new ConcurrentHashMap<>();
    public List<Bullet> bullets = new CopyOnWriteArrayList<>();


    public GameMap map = new GameMap();
}
