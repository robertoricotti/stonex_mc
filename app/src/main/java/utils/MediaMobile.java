package utils;

import java.util.ArrayDeque;
import java.util.Queue;

public class MediaMobile {
    private final int windowSize;
    private final Queue<Integer> dataQueue;
    private int sum;

    public MediaMobile(int windowSize) {
        this.windowSize = Math.max(windowSize, 1);
        dataQueue = new ArrayDeque<>();
        sum = 0;
    }

    public int addData(int newData) {
        dataQueue.add(newData);

        if (dataQueue.size() > windowSize) {
            dataQueue.poll();
        }
        sum = 0;
        for(Integer q : dataQueue)
            sum += q;

        return getAverage();
    }

    public int getAverage() {
        if (dataQueue.isEmpty()) {
            return 0;
        }

        return sum / dataQueue.size();
    }



}
