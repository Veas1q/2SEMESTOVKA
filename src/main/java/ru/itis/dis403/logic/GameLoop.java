package ru.itis.dis403.logic;

public class GameLoop extends Thread {

    private final Runnable onTick;

    public GameLoop(GameState state, Runnable onTick) {
        this.onTick = onTick;
    }

    @Override
    public void run() {
        while (true) {
            onTick.run();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
