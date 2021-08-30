package com.example.offlinemassage.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.example.offlinemassage.utils.ChatController;
import com.example.offlinemassage.ui.adapter.MessageAdapter;
import com.example.offlinemassage.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.util.DisplayMetrics;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {



    public static final  int REQ_ENABLE_BT = 1212;
    public static final  int PERM_REQ_CODE = 1234;



    Toolbar toolbar;
    Dialog dialog;
    FloatingActionButton fab;

    ChatController chatController;
    AppCompatTextView status;
    RecyclerView ChatList;

    ArrayAdapter<BluetoothDevice> discoveryAdapter;
    BroadcastReceiver discoveryReceiver;
    BluetoothAdapter bluetoothAdapter;

    AppCompatButton button;
    AppCompatEditText editText;

    MessageAdapter messageAdapter;
    Handler handler;

    BluetoothDevice connectingDevice;


    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initHandler();
    }




    public  void init(){

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        ChatList = findViewById(R.id.chat_list);
        status = findViewById(R.id.status);
        editText = findViewById(R.id.input);
        button = findViewById(R.id.send);
        messageAdapter = new MessageAdapter();
        ChatList.setLayoutManager(new LinearLayoutManager(this));


        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        if (bluetoothAdapter == null){
            Snackbar.make(fab, "Bluetooth is not available.", Snackbar.LENGTH_LONG).show();
        }

        fab.setOnClickListener(v -> showDevicesDialog());

        initReceiver();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERM_REQ_CODE);
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = editText.getText().toString().trim();
                if (text.isEmpty()){
                    return;
                }
                chatController.write(text.getBytes());
            }
        });
    }















    @Override
    protected void onResume() {
        super.onResume();
        if (! bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT);
        }else{
            chatController = new ChatController(handler);
        }
        if (chatController != null && chatController.getState() == ChatController.STATE_NONE){
            chatController.start();
        }
    }





    private void initReceiver() {

        discoveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    Toast.makeText(context, "device founded.", Toast.LENGTH_SHORT).show();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    discoveryAdapter.add(device);
                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                    Toast.makeText(context, "discovery finished." + discoveryAdapter.getCount() +
                            "items found.", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }





    private void initHandler(){

        handler = new Handler(msg -> {
            switch (msg.what){

                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1){

                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to " + connectingDevice.getName());
                            break;

                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting" );

                            break;

                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:

                            setStatus("not Connect!!!");

                            break;

                        default:break;
                    }

                    break;

                case MESSAGE_READ:
                    byte [] buffer =  (byte[]) msg.obj;
                    String readMessage = new String(buffer, 0, msg.arg1);
                    MainActivity.this.showReadMassage(readMessage);
                    break;


                case MESSAGE_WRITE:
                    byte [] writeBuffer =  (byte[]) msg.obj;
                    String WriteMessage = new String(writeBuffer);
                    MainActivity.this.showWriteMassage(WriteMessage);
                    break;


                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable("device");

                    if (connectingDevice != null) {
                      Snackbar.make(fab, "Connecting to "+ connectingDevice.getName(), Snackbar.LENGTH_LONG).show();
                    }
                    break;



                case MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;
                default:break;

            }
            return false;
        });

    }




    private void showWriteMassage(String WriteMessage) {

        messageAdapter.addMessage(WriteMessage, true);

    }





    private void showReadMassage( String readMessage) {
        messageAdapter.addMessage(readMessage, false);

    }





    private void showDevicesDialog(){

        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }


        dialog = new Dialog(this);
        dialog.setContentView(R.layout.devices_dialog);
        dialog.findViewById(R.id.text);
        ListView listView = dialog.findViewById(R.id.listview);

        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();

       discoveryAdapter =  new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1){

           @NonNull
           @Override
           public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

               View view = super.getView(position, convertView, parent);
               TextView textView = view.findViewById(android.R.id.text1);
               textView.setText(getItem(position).getName());
               return view;
           }

       };




        listView.setAdapter(discoveryAdapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);

         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, filter);



        dialog.setCancelable(false);

        dialog.setOnCancelListener(dialog -> bluetoothAdapter.cancelDiscovery());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            bluetoothAdapter.cancelDiscovery();
            chatController.connect(discoveryAdapter.getItem(position));
            dialog.dismiss();
        });

        resizeDialog(dialog);
        dialog.show();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }






    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_ENABLE_BT){
            if (resultCode == RESULT_OK){
                Snackbar.make(fab, "BT is active", Snackbar.LENGTH_SHORT).show();
                chatController = new ChatController(handler);
            }else {
                Snackbar.make(fab, "BT is still disabled", Snackbar.LENGTH_SHORT).show();
                Toast.makeText(this, "closing applications", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERM_REQ_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Snackbar.make(fab, "Permission Granted",Snackbar.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission denied, closing applications", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    private void resizeDialog(Dialog dialog){
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    Window window = dialog.getWindow();

    if (window != null){
        window.setLayout((int) (0.9 * metrics.widthPixels), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}





    public void setStatus(String st){
        status.setText(st);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatController != null){
            chatController.stop();
        }
    }
}