package timer;

import java.util.Timer;
import java.util.TimerTask;

public class CustomTimer extends TimerTask {
    public Timer UtilTimer;
    private int CurrentTicks = 0;
    private int TargetTicks;
    private TimerTask Task;
    private final int Speed = 1000;
    private final int Delay = 0;

    public CustomTimer(TimerTask task, int targetTicks) {
        TargetTicks = targetTicks;
        Task = task;
        UtilTimer = new Timer();
        UtilTimer.scheduleAtFixedRate(this, Delay, Speed);
    }

    @Override
    public void run() {
        CurrentTicks++;
        if (CurrentTicks == TargetTicks) {
            Task.run();
        }
    }

    public int getTicks() {
        return CurrentTicks;
    }

    public void resetTicks() {
        CurrentTicks = 0;
    }

}
