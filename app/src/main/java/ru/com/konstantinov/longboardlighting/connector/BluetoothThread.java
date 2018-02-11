package ru.com.konstantinov.longboardlighting.connector;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;

import ru.com.konstantinov.longboardlighting.LedMode;
import ru.com.konstantinov.longboardlighting.interfaces.ActionListener;
import ru.com.konstantinov.longboardlighting.interfaces.ConnectionInterface;

/**
 * Created by ceyler on 11.02.2018.
 * Thread for bluetooth communication
 */

public class BluetoothThread extends Thread implements ConnectionInterface {
    private final Scanner scanner;
    private final Writer writer;
    private final ActionListener listener;
    private final Object syncObject;

    private volatile LedMode mode = LedMode.RAINBOW_FADE;
    private volatile int brightness = 255;
    private volatile Color color;
    private volatile float voltage;

    BluetoothThread(@NotNull InputStream inputStream, @NotNull OutputStream outputStream, @NotNull ActionListener listener, @NotNull Object syncObject) {
        this.scanner = new Scanner(inputStream).useDelimiter("@");
        this.writer = new OutputStreamWriter(outputStream);
        this.listener = listener;
        this.syncObject = syncObject;
    }

    @Override
    public void run() {
        while (true) {
            String output = "#0:" + Integer.toString(this.mode.getCode()) + "#1:" + Integer.toString(this.brightness) + "@";

            try {
                writer.write(output);
                writer.flush();
                Log.w("Finder", "Writing: " + output);
            } catch (IOException e) {
                listener.onAction(BluetoothAdapter.STATE_DISCONNECTED);
                break;
            }

            Log.w("Finder", "Start reading");

            String result = scanner.next();
            char voltage[] = new char[4];
            result.getChars(3, 6, voltage, 0);
            Log.w("Finder", "Result: " + Arrays.toString(voltage));

            Log.w("Finder", "Finish reading");

            try {
                synchronized (syncObject) {
                    syncObject.wait(1000); // wait for a second
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void setMode(@NonNull LedMode mode) {
        this.mode = mode;
    }

    @Override
    public void setBrightness(int value) {
        this.brightness = value;
    }

    @Override
    public void setColor(@NonNull Color color) {
        this.color = color;
    }

    @Override
    public float getVoltage() {
        return this.voltage;
    }
}