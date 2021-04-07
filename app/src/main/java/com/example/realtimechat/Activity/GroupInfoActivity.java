package com.example.realtimechat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.realtimechat.Adapter.ParticipantAddAdapter;
import com.example.realtimechat.Model.User;
import com.example.realtimechat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId;
    private ActionBar actionBar;
    private TextView editGroupTv, addParticipentTv, leaveGroupTv, participantTv, descriptionTv, createdByTv;
    private RecyclerView participantRv;
    private String myGroupRole = "";
    private FirebaseAuth firebaseAuth;
    private ArrayList<User> userList;
    private ParticipantAddAdapter participantAddAdapter;
    private ImageView groupIconIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupId = getIntent().getStringExtra("groupId");
        getIds();
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();
        addParticipentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupparticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if (myGroupRole.equals("creator")) {
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure want to delete group permanently";
                    positiveButtonTitle = "DELETE";
                } else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure want to leave group permanently";
                    positiveButtonTitle = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("creator")) {
                                    //delete group
                                    deleteGroup();
                                } else {
                                    //leave group
                                    leaveGroup();
                                }
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();


            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group Left sccefuuly
                        Toast.makeText(GroupInfoActivity.this, "Group left successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed leave groupe
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group delete sccefuuly
                        Toast.makeText(GroupInfoActivity.this, "Group successfully deleted...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed delete groupe
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get Group details
                            String groupId = "" + ds.child("groupId").getValue();
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String GroupDesc = "" + ds.child("GroupDesc").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();

                            //   groupTitleTv.setText(groupTitle);

                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

                            loadCreatorInfo(dateTime, createdBy);

                            actionBar.setTitle(groupTitle);
                            descriptionTv.setText(GroupDesc);
                            if(ds.child("groupIcon").getValue().equals("")){
                                groupIconIv.setImageResource(R.drawable.groups);
                            }else {
                                Glide.with(GroupInfoActivity.this).load(groupIcon).into(groupIconIv);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCreatorInfo(String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("id").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("username").getValue();
                    createdByTv.setText("Created by " + name +" " + "on" +" "+ dateTime);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + "(" + myGroupRole + ")");
                            if (myGroupRole.equals("participants")) {
                                editGroupTv.setVisibility(View.GONE);
                                addParticipentTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave Group");
                            } else if (myGroupRole.equals("admin")) {
                                editGroupTv.setVisibility(View.GONE);
                                addParticipentTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave Group");
                            } else if (myGroupRole.equals("creator")) {
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipentTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Delete Group");
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadParticipants() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = "" + ds.child("uid").getValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("id").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                User user = ds.getValue(User.class);
                                //get All Usre
                                userList.add(user);

                            }
                            Log.d("==>", "==" + myGroupRole);
                            participantAddAdapter = new ParticipantAddAdapter(GroupInfoActivity.this, userList, groupId, myGroupRole);
                            participantRv.setAdapter(participantAddAdapter);
                            participantTv.setText("Participants (" + userList.size() + ")");

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


    private void getIds() {
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipentTv = findViewById(R.id.addParticipentTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantTv = findViewById(R.id.participantTv);
        participantRv = findViewById(R.id.participantRv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        groupIconIv=findViewById(R.id.groupIconIv);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}