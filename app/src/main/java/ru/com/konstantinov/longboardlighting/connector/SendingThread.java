package ru.com.konstantinov.longboardlighting.connector;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ru.com.konstantinov.longboardlighting.ControllerVariables;
import ru.com.konstantinov.longboardlighting.LedMode;
import ru.com.konstantinov.longboardlighting.interfaces.ActionListener;

/**
 * Created by ceyler on 11.02.2018.
 * Thread for sending data via Bluetooth
 */

public class SendingThread extends AsyncTask<Void, Integer, Void> {
    private final Writer writer;
    private final ActionListener listener;
    private final Object syncObject;

    private volatile LedMode mode = LedMode.RAINBOW_FADE;
    private volatile boolean isModeChanged = false;
    private volatile int brightness = 100;
    private volatile boolean isBrightnessChanged = false;

    SendingThread(@NotNull OutputStream outputStream, @NotNull ActionListener listener, @NotNull Object syncObject) {
        this.writer = new OutputStreamWriter(outputStream);
        this.listener = listener;
        this.syncObject = syncObject;
    }

    void setMode(@NonNull LedMode mode) {
        this.isModeChanged = true;
        this.mode = mode;
    }

    void setBrightness(int value) {
        this.isBrightnessChanged = true;
        this.brightness = value;
    }

    void setColor(@NonNull Color color) {
        throw new UnsupportedOperationException("Not ready yet :(");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (true) {
            StringBuilder output = new StringBuilder("");
            if (this.isModeChanged) {
                output.append('#').append(ControllerVariables.MODE.getCode()).append(':').append(this.mode.getCode());
                this.isModeChanged = false;
            }
            if (this.isBrightnessChanged) {
                output.append('#').append(ControllerVariables.BRIGHTNESS.getCode()).append(':').append(this.brightness);
                this.isBrightnessChanged = false;
            }
            output.append("#3:3@");

            try {
                writer.write(output.toString());
                writer.flush();
                Log.w("LBSending", "Sent: " + output.toString());
            } catch (IOException e) {
                this.publishProgress(BluetoothAdapter.STATE_DISCONNECTED);
                break;
            }

            try {
                synchronized (syncObject) {
                    syncObject.wait(1000*30); // wait for a sec or a data update
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onAction(values[0]);
    }
}
