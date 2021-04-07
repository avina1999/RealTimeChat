package com.example.realtimechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realtimechat.Activity.GroupChatActivity;
import com.example.realtimechat.Activity.GroupInfoActivity;
import com.example.realtimechat.Model.GroupModel;
import com.example.realtimechat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupdAdapter extends RecyclerView.Adapter<GroupdAdapter.HolderGroupdAdapter> {
    private Context context;
    private ArrayList<GroupModel> groupModels;

    public GroupdAdapter(Context context, ArrayList<GroupModel> groupModels) {
        this.context = context;
        this.groupModels = groupModels;
    }

    @NonNull
    @Override
    public GroupdAdapter.HolderGroupdAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupschat_list, parent, false);
        return new HolderGroupdAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupdAdapter.HolderGroupdAdapter holder, int position) {

        GroupModel model = groupModels.get(position);
        String groupId = model.getGroupId();
        String groupTitle = model.getGroupTitle();
        String groupDesc = model.getGroupDesc();
        String groupIncon=model.getGroupIcon();
        holder.nameTv.setText("");
        holder.timetv.setText("");
        holder.messagetv.setText("");
        //load last message and message time

        loadLastMessage(holder, model);
        holder.groupTitle.setText(groupTitle);
       if(model.getGroupIcon().equals("")){
           holder.groupicon.setImageResource(R.mipmap.ic_launcher);
       }else {
           Glide.with(context).load(groupIncon).into(holder.groupicon);
       }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groups", groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(HolderGroupdAdapter holder, GroupModel model) {

        //get last message fro group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String message = "" + ds.child("message").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String sender = "" + ds.child("sender").getValue();
                            //
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy", cal).toString();
                            holder.messagetv.setText(message);
                            holder.timetv.setText(dateTime);
                            //get info sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("id").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String name = "" + ds.child("username").getValue();
                                                holder.nameTv.setText(name);

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
    public int getItemCount() {
        return groupModels.size();
    }

    class HolderGroupdAdapter extends RecyclerView.ViewHolder {

        private ImageView groupicon;
        private TextView groupTitle, nameTv, messagetv, timetv;

        private HolderGroupdAdapter(View itemView) {
            super(itemView);
            groupicon = itemView.findViewById(R.id.groupIcon);
            groupTitle = itemView.findViewById(R.id.groupTitle);
            nameTv = itemView.findViewById(R.id.nameTv);
            messagetv = itemView.findViewById(R.id.messagetv);
            timetv = itemView.findViewById(R.id.timetv);

        }
    }

}
