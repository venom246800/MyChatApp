package com.hackervenom.mychatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chats extends Fragment {
    private View PrivateChatView;
    private RecyclerView chatList;
    private DatabaseReference ChatRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUSerID;


    public Chats() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUSerID = mAuth.getCurrentUser().getUid();
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUSerID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("USERS");

        chatList = (RecyclerView) PrivateChatView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  PrivateChatView;

    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Conts> options =
                new FirebaseRecyclerOptions.Builder<Conts>()
                .setQuery(ChatRef, Conts.class)
                .build();

        FirebaseRecyclerAdapter<Conts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Conts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Conts model) {
                        final String userIDs = getRef(position).getKey();
                        final String[] reImage = {"default_image"};
                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("image"))
                                    {
                                        reImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(reImage[0]).into(holder.profileImg);
                                    }
                                    final String reName = dataSnapshot.child("name").getValue().toString();
                                    final String reStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(reName);
                                    holder.userStatus.setText("Last Seen: "+"\n"+"Date "+"Time");
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",userIDs);
                                            chatIntent.putExtra("visit_user_name",reName);
                                            chatIntent.putExtra("visit_image", reImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class  ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImg ;
        TextView userStatus, userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.users_status);
            userName = itemView.findViewById(R.id.users_profile_name);
        }
    }
}
