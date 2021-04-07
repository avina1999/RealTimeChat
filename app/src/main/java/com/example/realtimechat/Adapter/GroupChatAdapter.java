package com.example.realtimechat.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimechat.Model.GroupChatModel;
import com.example.realtimechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.HolderGroupChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private ArrayList<GroupChatModel> argroupChatModels;

    private FirebaseAuth firebaseAuth;


    public GroupChatAdapter(Context context, ArrayList<GroupChatModel> groupChatModels) {
        this.context = context;
        this.argroupChatModels = groupChatModels;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {

        GroupChatModel groupChatModel = argroupChatModels.get(position);
        String message = groupChatModel.getMessage();
        String SenderUid = groupChatModel.getSender();
        String timestamp = groupChatModel.getTimestamp();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm:aa",calendar).toString();
        Log.d("==>","==>"+message);
        holder.messageTv1.setText(message);
        holder.timeTv1.setText(dateTime);
        setUsername(groupChatModel, holder);

    }

    private void setUsername(GroupChatModel groupChatModel, HolderGroupChat holder) {
        //get send info fro uid in model

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("id").equalTo(groupChatModel.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("username").getValue();
                            Log.d("=>=",">>>"+name);
                           holder.nameTv1.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return argroupChatModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (argroupChatModels.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {
        private TextView nameTv1, messageTv1, timeTv1;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            nameTv1 = itemView.findViewById(R.id.nameTv1);
            messageTv1 = itemView.findViewById(R.id.messageTv1);
            timeTv1 = itemView.findViewById(R.id.timeTv1);

        }
    }
}
