package com.example.realtimechat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimechat.Model.User;
import com.example.realtimechat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ParticipantAddAdapter extends RecyclerView.Adapter<ParticipantAddAdapter.ViewHolder> {
    private Context context;
    private ArrayList<User> alUser;
    private String groupId, myGroupRole;

    public ParticipantAddAdapter(Context context, ArrayList<User> alUser, String groupId, String myGroupRole) {
        this.context = context;
        this.alUser = alUser;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rew_participant_add, parent, false);
        return new ParticipantAddAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = alUser.get(position);
        String id = user.getId();
        holder.participant_name.setText(user.getUsername());

        chechIfAlreadyExists(user, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("participants").child(id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    //user exist/not participant
                                    String previousRole = "" + snapshot.child("role").getValue();

                                    String[] option;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("creactor")) {
                                        if (previousRole.equals("admin")) {
                                            option = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        removeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        } else if (previousRole.equals("participants")) {
                                            option = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        makeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if (myGroupRole.equals("admin")) {
                                        if (previousRole.equals("creator")) {
                                            Toast.makeText(context, "Creator of Group....", Toast.LENGTH_SHORT).show();
                                        } else if (previousRole.equals("creator")) {
                                            option = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        removeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        } else if (previousRole.equals("participants")) {
                                            option = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        makeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }

                                } else {
                                    //user exist/not participant
                                    String[] option;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(" Add participants")
                                            .setMessage("Add this user")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addParticipant(user);
                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });
    }

    private void addParticipant(User user) {
        //setup user data
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getId());
        hashMap.put("role", "participants");
        hashMap.put("timestamp", timestamp);

        //add user in group>groupId>participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getId()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //add successful
                        Toast.makeText(context, "Added successesfully", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed adding user in group
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(User user) {
        //set data
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //add successful
                        Toast.makeText(context, "The user now admin", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed adding user in group
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeParticipant(User user) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //add successful
                        Toast.makeText(context, "The user now admin", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed adding user in group
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(User user) {
        //setdata=remove admin-just change role
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participants");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //add successful
                        Toast.makeText(context, "The user no longer admin", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed adding user in group
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chechIfAlreadyExists(User user, ViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child("participants").child(user.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //alrady exist
                            String hisRole = "" + snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        } else {
                            //doesn't exists
                            holder.statusTv.setText("");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @Override
    public int getItemCount() {
        return alUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView participant_name, statusTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            participant_name = itemView.findViewById(R.id.participant_name);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
