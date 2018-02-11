package ru.com.konstantinov.longboardlighting;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import ru.com.konstantinov.longboardlighting.connector.Finder;
import ru.com.konstantinov.longboardlighting.interfaces.ActionListener;
import ru.com.konstantinov.longboardlighting.interfaces.ActivityResultSubscriber;
import ru.com.konstantinov.longboardlighting.interfaces.DeviceFinder;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("UseSparseArrays")
    private Map<ActivityResultSubscriber, Integer> subscribers = new HashMap<>();

    private View mode_list;
    private View device_list;
    private TextView headerText;
    private DeviceFinder deviceFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.device_list = findViewById(R.id.devices_list_view);
        this.mode_list = findViewById(R.id.modes_list_view);
        this.headerText = findViewById(R.id.headerText);

        this.headerText.setText(R.string.action_devices);

        //TODO check connection state of device, if not connected then hide
        CircularProgressBar batteryProgressBar = findViewById(R.id.battery_progress_bar);
        batteryProgressBar.setColor(ContextCompat.getColor(this, R.color.cpb_progressbar_color));
        batteryProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.cpb_background_progressbar_color));
        batteryProgressBar.setProgressBarWidth(getResources().getDimension(R.dimen.cpb_progressbar_width));
        batteryProgressBar.setBackgroundProgressBarWidth(getResources().getDimension(R.dimen.cpb_background_progressbar_width));
        int animationDuration = 1500; // 2500ms = 2,5s
        batteryProgressBar.setProgressWithAnimation(66, animationDuration); // Default duration = 1500ms

        final CircularProgressBar connectionStatus = findViewById(R.id.connection_status_bar);
        connectionStatus.setColor(ContextCompat.getColor(this, R.color.connection_color));
        connectionStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.background_connection_color));
        connectionStatus.setProgressBarWidth(getResources().getDimension(R.dimen.connection_width));
        connectionStatus.setBackgroundProgressBarWidth(getResources().getDimension(R.dimen.background_connection_width));
//        int animationDuration = 1500; // 2500ms = 2,5s



//        TODO complete handler
        this.deviceFinder = new Finder(this, new ActionListener() {
            @Override
            public void onAction(int action) {
                switch (action) {
                    case BluetoothAdapter.ERROR:
                        break;
                    case BluetoothAdapter.STATE_ON:
//                        set indicator green
                        connectionStatus.setProgressWithAnimation(100, 1); // Default duration = 1500ms

                        break;
                    case BluetoothAdapter.STATE_OFF:

                        break;
                    default:
                        connectionStatus.setProgressWithAnimation(0, 1); // Default duration = 1500ms

                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_devices:
                this.device_list.setVisibility(View.VISIBLE);
                this.mode_list.setVisibility(View.GONE);
                this.headerText.setText(R.string.action_devices);
                break;
            case R.id.action_modes:
                this.device_list.setVisibility(View.GONE);
                this.mode_list.setVisibility(View.VISIBLE);
                this.headerText.setText(R.string.action_modes);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void subscribeOnActivityResult(@NotNull ActivityResultSubscriber subscriber, int requestCode) {
        subscribers.put(subscriber, requestCode);
    }

    public void unsubscribeFromActivityResult(@NotNull ActivityResultSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (ActivityResultSubscriber subscriber : subscribers.keySet()) {
            if (subscribers.get(subscriber) == requestCode)
                subscriber.onActivityResult(resultCode, data); // call all subscribers with this request code
        }
    }

    @Override
    protected void onDestroy() {
        this.deviceFinder.onActivityDestroy(this);
        super.onDestroy();
    }
}
