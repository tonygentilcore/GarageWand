package tonyg.com.garagewand;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "GarageWand";
    private final BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, String.valueOf(device));
        }
    };

    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @SuppressLint("Override")
        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            final String message = "Advertisement successful";
            Log.d(TAG, message);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }

        @SuppressLint("Override")
        @Override
        public void onStartFailure(int i) {
            final String message = "Advertisement failed error code: " + i;
            Log.e(TAG, message);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }

    };
    private static final ParcelUuid GARAGE_WAND_UUID =
            ParcelUuid.fromString("2f66fb49-55f1-43fc-a3bb-2b73593bfa8f");
    private BluetoothAdapter mBluetoothAdapter;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothManager.openGattServer(getApplicationContext(), mBluetoothGattServerCallback);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Log.e(TAG, "Device doesn't support multiple advertisements!");
            return;
        }

        BluetoothLeAdvertiser advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            Log.e(TAG, "Unable to advertise on this device!");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setConnectable(true)
                .build();
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .addServiceUuid(GARAGE_WAND_UUID)
                .setIncludeTxPowerLevel(false)
                .build();

        advertiser.startAdvertising(settings, advertiseData, mAdvertiseCallback);
    }
}
