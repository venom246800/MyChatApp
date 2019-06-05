package com.hackervenom.mychatapp;


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
public class Contacts extends Fragment {

    private View ContactsView;
    private RecyclerView myContactList;

    private DatabaseReference ContactsRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public Contacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UserRef = FirebaseDatabase.getInstance().getReference().child("USERS");
        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Conts>()
                .setQuery(ContactsRef,Conts.class)
                .build();

        final FirebaseRecyclerAdapter<Conts,ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Conts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Conts model) {
                        String userIDs = getRef(position).getKey();
                        UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("image"))
                                {
                                    String userImage = dataSnapshot.child("image").getValue().toString();
                                    String ProfileName = dataSnapshot.child("name").getValue().toString();
                                    String ProfileStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(ProfileName);
                                    holder.userStatus.setText(ProfileStatus);
                                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImg);
                                }
                                else {
                                    String ProfileName = dataSnapshot.child("name").getValue().toString();
                                    String ProfileStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(ProfileName);
                                    holder.userStatus.setText(ProfileStatus);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImg;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.users_profile_name);
            userStatus = itemView.findViewById(R.id.users_status);
            profileImg = itemView.findViewById(R.id.users_profile_image);

        }
    }
}
