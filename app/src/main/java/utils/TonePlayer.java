package utils;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class TonePlayer {

    public enum Mode {
        OFF,
        CONTINUOUS,
        INTERMITTENT_100MS,
        INTERMITTENT_400MS
    }

    private static final int SAMPLE_RATE = 44100;
    private static final double DEFAULT_FREQ = 1800.0;
    private static final double DEFAULT_VOLUME = 0.25; // 0.0 - 1.0

    private AudioTrack audioTrack;
    private Thread audioThread;
    private volatile boolean running = false;
    private volatile Mode currentMode = Mode.OFF;

    private final double frequency;
    private final double volume;

    public TonePlayer() {
        this(DEFAULT_FREQ, DEFAULT_VOLUME);
    }

    public TonePlayer(double frequency, double volume) {
        this.frequency = frequency;
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    public synchronized void start() {
        if (running) return;

        int minBuffer = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                Math.max(minBuffer, SAMPLE_RATE / 2),
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        running = true;
        audioTrack.play();

        audioThread = new Thread(this::audioLoop, "TonePlayerThread");
        audioThread.start();
    }

    public synchronized void stop() {
        running = false;
        currentMode = Mode.OFF;

        if (audioThread != null) {
            try {
                audioThread.join(300);
            } catch (InterruptedException ignored) {
            }
            audioThread = null;
        }

        if (audioTrack != null) {
            try {
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.release();
            } catch (Exception ignored) {
            }
            audioTrack = null;
        }
    }

    public void setMode(Mode mode) {
        currentMode = mode;
    }

    private void audioLoop() {
        int chunkMs = 20;
        int samplesPerChunk = SAMPLE_RATE * chunkMs / 1000;

        short[] toneBuffer = generateTone(samplesPerChunk, frequency, volume);
        short[] silenceBuffer = new short[samplesPerChunk];

        long phaseTimeMs = 0;
        boolean soundOn = true;

        while (running) {
            Mode mode = currentMode;

            if (mode == Mode.OFF) {
                writeSafely(silenceBuffer);
                continue;
            }

            int periodMs;
            switch (mode) {
                case CONTINUOUS:
                    writeSafely(toneBuffer);
                    continue;

                case INTERMITTENT_100MS:
                    periodMs = 100;
                    break;

                case INTERMITTENT_400MS:
                    periodMs = 400;
                    break;

                default:
                    writeSafely(silenceBuffer);
                    continue;
            }

            if (soundOn) {
                writeSafely(toneBuffer);
            } else {
                writeSafely(silenceBuffer);
            }

            phaseTimeMs += chunkMs;
            if (phaseTimeMs >= periodMs) {
                phaseTimeMs = 0;
                soundOn = !soundOn;
            }
        }
    }

    private void writeSafely(short[] buffer) {
        if (audioTrack != null) {
            audioTrack.write(buffer, 0, buffer.length);
        }
    }

    private short[] generateTone(int numSamples, double freq, double vol) {
        short[] buffer = new short[numSamples];
        double twoPiF = 2.0 * Math.PI * freq;

        for (int i = 0; i < numSamples; i++) {
            double t = i / (double) SAMPLE_RATE;
            double sample = Math.sin(twoPiF * t) * vol;
            buffer[i] = (short) (sample * Short.MAX_VALUE);
        }

        return buffer;
    }
}