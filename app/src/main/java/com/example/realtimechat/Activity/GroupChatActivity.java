package com.example.realtimechat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.realtimechat.Adapter.GroupChatAdapter;
import com.example.realtimechat.Model.GroupChatModel;
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
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar toolbar1;
    private String groupId, myGroupRole = "";
    private ImageView groupIconIv;
    private EditText messagezEt;
    private ImageButton attechBtn, sendBtn;
    private TextView groupTitleTv, add_participent;
    private FirebaseAuth firebaseAuth;
    private RecyclerView chatRv;
    private ActionBar actionBar;

    private ArrayList<GroupChatModel> arGroupChatList;
    private GroupChatAdapter groupChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Group");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getIds();
    }

    private void getIds() {
        groupIconIv = findViewById(R.id.groupIconIv);
        messagezEt = findViewById(R.id.messagezEt);
        attechBtn = findViewById(R.id.attechBtn);
        sendBtn = findViewById(R.id.sendBtn);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        chatRv = findViewById(R.id.chatRv);
        add_participent = findViewById(R.id.add_participent);
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groups");
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupsInfo();
        loadGroupChatMessages();
        loadMyGropRole();
//        add_participent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GroupChatActivity.this, GroupparticipantAddActivity.class);
//                intent.putExtra("groupId", groupId);
//                startActivity(intent);
//            }
        //  });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String message = messagezEt.getText().toString().trim();
                //Text Utils
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Can't send empty messages", Toast.LENGTH_SHORT).show();
                } else {
                    //send message

                    SendMessages(message);
                }
            }
        });
    }

    private void loadMyGropRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadGroupChatMessages() {
        arGroupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arGroupChatList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            GroupChatModel model = ds.getValue(GroupChatModel.class);
                            arGroupChatList.add(model);
                        }
                        groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, arGroupChatList);
                        chatRv.setAdapter(groupChatAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendMessages(String message) {
        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);

        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");//text/image/file
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        //
                        messagezEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //message sending fail
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadGroupsInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String GroupDesc = "" + ds.child("GroupDesc").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();
                            String Profileimge = "" + ds.child("groupIcon").getValue();
                            groupTitleTv.setText(groupTitle);
                            if (ds.child("groupIcon").getValue().equals("")) {
                                groupIconIv.setImageResource(R.mipmap.ic_launcher);
                            } else {
                                Glide.with(GroupChatActivity.this).load(Profileimge).into(groupIconIv);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
//        ref.orderByChild("group Id").equalTo(groupId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for(DataSnapshot ds:snapshot.getChildren()){
//                            String groupTitkle
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                })
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.logOut).setVisible(false);
        menu.findItem(R.id.group).setVisible(false);
        if (myGroupRole.equals("creator") || (myGroupRole.equals("admin"))) {
            menu.findItem(R.id.addGroupParticipent).setVisible(true);
        } else {
            menu.findItem(R.id.addGroupParticipent).setVisible(false);

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addGroupParticipent:
                Intent intent = new Intent(GroupChatActivity.this, GroupparticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                return true;
            case R.id.action_groupinfo:
                Intent intent1 = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intent1.putExtra("groupId", groupId);
                startActivity(intent1);
                return true;

        }
        return false;
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}