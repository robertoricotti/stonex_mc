package utils;

import java.util.ArrayDeque;
import java.util.Queue;

public class SpikeFilter {
    private final int windowSize;
    private final Queue<Double> dataQueue;
    private double sum;

    public SpikeFilter(int windowSize) {
        this.windowSize = Math.max(windowSize, 1);
        dataQueue = new ArrayDeque<>();
        sum = 0;
    }

    public double addData(double newData, double threshold) {
        dataQueue.add(newData);
        sum += newData;

        if (dataQueue.size() > windowSize) {
            double removedData = dataQueue.poll();
            sum -= removedData;
        }

        double average = getAverage();
        double difference = Math.abs(newData - average);

        if (difference <= threshold) {
            return average;
        } else {
            return newData;
        }
    }

    public double getAverage() {
        if (dataQueue.isEmpty()) {
            return 0;
        }

        return sum / Math.min(dataQueue.size(), windowSize);
    }


}
