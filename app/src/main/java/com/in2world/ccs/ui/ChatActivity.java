package com.in2world.ccs.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.in2world.ccs.Database.SaveData;
import com.in2world.ccs.LoginActivity;
import com.in2world.ccs.R;
import com.in2world.ccs.adapters.MessageListAdapter;
import com.in2world.ccs.module.Data;
import com.in2world.ccs.module.Message;
import com.in2world.ccs.socket.SocketClient;
import com.in2world.ccs.socket.SocketIO;
import com.in2world.ccs.tools.GlobalData;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private View separator;
    private RecyclerView myRecylerView;
    private EditText messagetxt;
    private Button send;
    MessageListAdapter messageListAdapter;
    //declare socket object
    private SocketClient socket;
    public List<String> MessageList ;
    MediaPlayer mPlayer;

    public static String Nickname ;
    private void initView() {
        separator = (View) findViewById(R.id.separator);
        myRecylerView =  findViewById(R.id.messagelist);
        messagetxt = (EditText) findViewById(R.id.message);
        send = (Button) findViewById(R.id.send);
        mPlayer = MediaPlayer.create(this, R.raw.new_message);

        initSocket();
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();

    }



    private void init() {
        //setting up recyler
        MessageList = new ArrayList<>();
        myRecylerView =  findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());
        messageListAdapter = new MessageListAdapter(this, MessageList);
        myRecylerView.setAdapter(messageListAdapter);


        messagetxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d(TAG, "onTextChanged: start "+start);
                Log.d(TAG, "onTextChanged: before "+before);
                Log.d(TAG, "onTextChanged: count "+count);
                try {
                    if (count != 0){
                        JSONObject dataJSON = new JSONObject();
                        dataJSON.put("username",GlobalData.mProfile.getUsername());
                        Log.d(TAG, "LogIn: data "+dataJSON.toString());
                        SocketIO.getInstance().getSocket().emit("borad", dataJSON);


                    }else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //message send action
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the nickname and the message content and fire the event messagedetection


                if(!messagetxt.getText().toString().isEmpty()){

                    //  Message message = new Message(Nickname,messagetxt.getText().toString(),1,0,0,""+System.currentTimeMillis());

                    //  Data data = new Data("MESSAGE",message);


                    TestMessage message = new TestMessage(GlobalData.mProfile.getUsername(),messagetxt.getText().toString());



                    String data = message.username+":"+message.message;


                    //MessageList.add(data);


                    try {
                        sendMessage(message);


                        // playNotificationSound();
                        // messageListAdapter.notifyDataSetChanged();
                        // if (messageListAdapter.getItemCount() > 1)
                        //     myRecylerView.getLayoutManager().smoothScrollToPosition(myRecylerView, null, messageListAdapter.getItemCount() - 1);

                        messagetxt.setText(" ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        });

    }

    private void sendMessage(TestMessage data) throws JSONException {

        SaveData.getInstance().saveObject("object",data);
        String json = SaveData.getInstance().getString("object");
        JSONObject dataJSON = new JSONObject();
        dataJSON.put("username",data.username);
        dataJSON.put("message",data.message);
        Log.d(TAG, "LogIn: json "+json);
        Log.d(TAG, "LogIn: data "+dataJSON.toString());
        SocketIO.getInstance().getSocket().emit("message", dataJSON);

    }

    public void onMessage(Data data) {
    /*    MessageList.add(data.getMessage());
        playNotificationSound();
        messageListAdapter.notifyDataSetChanged();
        if (messageListAdapter.getItemCount() > 1)
            myRecylerView.getLayoutManager().smoothScrollToPosition(myRecylerView, null, messageListAdapter.getItemCount() - 1);
 */
    }

    public void playNotificationSound() {

        if (mPlayer == null)
            return;

        if (mPlayer.isPlaying())
            mPlayer.stop();

        mPlayer.start();
    }


    private void initSocket() {

        Emitter.Listener onNewMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        Log.e(TAG, "run: "+data.toString() );
                        String username;
                        String message;
                        try {
                            username = data.getString("username");
                            message = data.getString("message");

                            String messaged = username+":"+message;

                            MessageList.add(messaged);
                            playNotificationSound();
                            messageListAdapter.notifyDataSetChanged();
                            if (messageListAdapter.getItemCount() > 1)
                                myRecylerView.getLayoutManager().smoothScrollToPosition(myRecylerView, null, messageListAdapter.getItemCount() - 1);

                        } catch (JSONException e) {
                            return;
                        }


                    }
                });
            }
        };
        SocketIO.getInstance().getSocket().on("new_msg",onNewMessage);
    }

    class TestMessage{
        String username = "";
        String message = "";

        public TestMessage(String username, String message) {
            this.username = username;
            this.message = message;
        }
    }
}
