package com.example.realtimechat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.realtimechat.Adapter.ParticipantAddAdapter;
import com.example.realtimechat.Model.User;
import com.example.realtimechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupparticipantAddActivity extends AppCompatActivity {
    private RecyclerView user_Rv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole;

    private ArrayList<User> userList;
    private ParticipantAddAdapter participantAddAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupparticipant_add);

        actionBar = getSupportActionBar();
       // actionBar.setTitle("Add Participants");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user_Rv = findViewById(R.id.user_Rv);

        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();
    }

    private void getAllUser() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user=ds.getValue(User.class);


                    //get All Usre
                    Log.d("==>","==>"+firebaseAuth.getUid());
                    Log.d("==>","==>"+user.getId());


                    if(!firebaseAuth.getUid().equals(user.getId())){
                        userList.add(user);
                    }
                }
                Log.d("==>","=="+myGroupRole);
                participantAddAdapter=new ParticipantAddAdapter(GroupparticipantAddActivity.this,userList,""+groupId,""+myGroupRole);
                user_Rv.setAdapter(participantAddAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadGroupInfo() {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String groupId = "" + ds.child("groupId").getValue();
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String GroupDesc = "" + ds.child("GroupDesc").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();
                            actionBar.setTitle("Add Participants");
                            ref1.child(groupId).child("participants").child(firebaseAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                myGroupRole = "" + snapshot.child("role").getValue();
                                                actionBar.setTitle(groupTitle + "(" + myGroupRole + ")");

                                                getAllUser();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}