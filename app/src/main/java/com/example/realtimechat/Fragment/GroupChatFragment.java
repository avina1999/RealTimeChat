package com.example.realtimechat.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.realtimechat.Adapter.GroupdAdapter;
import com.example.realtimechat.Model.GroupModel;
import com.example.realtimechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupChatFragment extends Fragment {
    private RecyclerView groupRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<GroupModel> groupModelArrayList;
    private GroupdAdapter groupdAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        groupRv = view.findViewById(R.id.groupRv);
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupChatList();

        return view;
    }

    private void loadGroupChatList() {
        groupModelArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupModelArrayList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    // if current udi exists in participantsn list of groups  then show that group
                    Log.d("==>","==>"+firebaseAuth.getUid());
                    if(snapshot.child("participants").child(firebaseAuth.getUid()).exists()){

                            GroupModel model = snapshot.getValue(GroupModel.class);
                            groupModelArrayList.add(model);

                    }
                }
                groupdAdapter=new GroupdAdapter(getActivity(),groupModelArrayList);
                groupRv.setAdapter(groupdAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
//    private void searchGroupChatList() {
//        groupModelArrayList = new ArrayList<>();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                groupModelArrayList.size();
//                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
//                    if(snapshot.child("participants").child(firebaseAuth.getUid()).exists()){
//                        if(snapshot.child("groupTitle").toString().toLowerCase().contains())
//                        GroupModel model=snapshot.getValue(GroupModel.class);
//                        groupModelArrayList.add(model);
//                    }
//                }
//                groupdAdapter=new GroupdAdapter(getActivity(),groupModelArrayList);
//                groupRv.setAdapter(groupdAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
}