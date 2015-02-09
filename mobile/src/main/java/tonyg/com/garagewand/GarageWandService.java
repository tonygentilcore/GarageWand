package tonyg.com.garagewand;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class GarageWandService extends IntentService {

    private static final String TAG = "GarageWand";

    private static final UUID GARAGE_WAND_UUID =
            UUID.fromString("2f66fb49-55f1-43fc-a3bb-2b73593bfa8f");

    private final Thread mServerThread = new Thread() {
        public void run() {
            BluetoothServerSocket serverSocket = null;
            BluetoothSocket socket = null;
            final BluetoothManager manager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            final BluetoothAdapter adapter = manager.getAdapter();

            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                        "tony secure", GARAGE_WAND_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    new Thread(new CommunicationThread(socket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public GarageWandService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        switch (intent.getAction()) {
            case "OPEN":
                Log.d(TAG, "Open");
                break;
            default:
                mServerThread.start();
        }
    }

    private final class CommunicationThread implements Runnable {
        private final BluetoothSocket clientSocket;
        private BufferedReader input = null;

        public CommunicationThread(BluetoothSocket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    Log.d(TAG, read);

                    final Intent openIntent = new Intent(getApplicationContext(),
                            GarageWandService.class).setAction("OPEN");

                    Notification.Builder mBuilder =
                            new Notification.Builder(getApplicationContext())
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle("GarageWand")
                                    .setContentText(read)
                                    .setCategory(Notification.CATEGORY_SERVICE)
                                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                                    .setPriority(Notification.PRIORITY_MAX)
                                    .addAction(R.drawable.ic_launcher, "Open", PendingIntent.getService(getApplicationContext(),
                                            0,
                                            openIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT));

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, mBuilder.build());
                } catch (IOException e) {
                    break;
                }
            }
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
