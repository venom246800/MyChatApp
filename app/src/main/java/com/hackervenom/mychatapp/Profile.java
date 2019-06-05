package com.hackervenom.mychatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private String receiverUserId, senderUserID, Current_State;
    private CircleImageView userProfileImg;
    private TextView userProfileName, userProfileStatus;
    private Button SendMsgReqBtn, DeclineMsgReqBtn;
    private DatabaseReference UserRef, ChatReqRef, ContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("USERS");
        ChatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();


        userProfileImg = (CircleImageView) findViewById(R.id.visit_profile_img);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMsgReqBtn = (Button) findViewById(R.id.send_msg_req_btn);
        DeclineMsgReqBtn = (Button) findViewById(R.id.decline_msg_req_btn);
        Current_State = "new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();


                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImg);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatReq();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatReq();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatReq() {

        ChatReqRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("send"))
                            {
                                Current_State = "request_send";
                                SendMsgReqBtn.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received"))
                            {
                                Current_State = "request_received";
                                SendMsgReqBtn.setText("Accept ChatRequest");
                                DeclineMsgReqBtn.setVisibility(View.VISIBLE);
                                DeclineMsgReqBtn.setEnabled(true);
                                DeclineMsgReqBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatReq();
                                    }
                                });
                            }
                        }
                        else {
                            ContactsRef.child(senderUserID)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserId))
                                            {
                                                Current_State = "friends";
                                                SendMsgReqBtn.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if(!senderUserID.equals(receiverUserId))
        {
            SendMsgReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMsgReqBtn.setEnabled(false);
                    if(Current_State.equals("new"))
                    {
                        SendChatReq();
                    }
                    if(Current_State.equals("request_send"))
                    {
                        CancelChatReq();
                    }
                    if(Current_State.equals("request_received"))
                    {
                        AcceptChatReq();
                    }
                    if(Current_State.equals("friends"))
                    {
                        RemoveThisContact();
                    }
                }
            });
        }
        else {
            SendMsgReqBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveThisContact() {
        ContactsRef.child(senderUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                SendMsgReqBtn.setEnabled(true);
                                                Current_State = "new";
                                                SendMsgReqBtn.setText("Send Request");
                                                DeclineMsgReqBtn.setVisibility(View.INVISIBLE);
                                                DeclineMsgReqBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatReq() {
        ContactsRef.child(senderUserID).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserId).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                ChatReqRef.child(senderUserID).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    ChatReqRef.child(receiverUserId).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    SendMsgReqBtn.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    SendMsgReqBtn.setText("Remove this Contact");
                                                                                    DeclineMsgReqBtn.setVisibility(View.INVISIBLE);
                                                                                    DeclineMsgReqBtn.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatReq() {
        ChatReqRef.child(senderUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatReqRef.child(receiverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                SendMsgReqBtn.setEnabled(true);
                                                Current_State = "new";
                                                SendMsgReqBtn.setText("Send Request");
                                                DeclineMsgReqBtn.setVisibility(View.INVISIBLE);
                                                DeclineMsgReqBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void SendChatReq() {
        ChatReqRef.child(senderUserID).child(receiverUserId)
                .child("request_type").setValue("send")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatReqRef.child(receiverUserId).child(senderUserID)
                                    .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        SendMsgReqBtn.setEnabled(true);
                                        Current_State = "request_send";
                                        SendMsgReqBtn.setText("Cancel Chat Request");
                                    }
                                }
                            });

                        }
                    }
                });
    }
}
