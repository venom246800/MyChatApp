package com.hackervenom.mychatapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChat extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton sendMsgButton;
    private EditText userMsgInput;
    private ScrollView mScrollView;
    private TextView displayTextMsg;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,GroupNameRef, GroupMsgKeyRef;
    private String currentGroupName , currentUserName , currentUserId, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("USERS");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        mToolbar = (Toolbar) findViewById(R.id.group_app_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMsgButton = (ImageButton) findViewById(R.id.send_msg_button);
        userMsgInput = (EditText) findViewById(R.id.input_group_msg);
        displayTextMsg = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);

        GetUserInfo();

        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMsgInfoToDatabase();
                userMsgInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GroupNameRef .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMsg = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
            displayTextMsg.append(chatName+":\n"+chatMsg+"\n"+chatTime+"  "+chatDate+"\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void saveMsgInfoToDatabase() {
        String msg = userMsgInput.getText().toString();
        String msgKey = GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(msg))
        {
            Toast.makeText(GroupChat.this,"Enter msg First",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMsgKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMsgKey);

            GroupMsgKeyRef = GroupNameRef.child(msgKey);

            HashMap<String,Object> msgInfoMap = new HashMap<>();
            msgInfoMap.put("name",currentUserName);
            msgInfoMap.put("msg",msg);
            msgInfoMap.put("date",currentDate);
            msgInfoMap.put("time",currentTime);
            GroupMsgKeyRef.updateChildren(msgInfoMap);
        }
    }

    private void GetUserInfo()
    {
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
