package com.example.realtimechat.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.realtimechat.Model.User;
import com.example.realtimechat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private CircleImageView image_profile;
    private TextView username;
    private DatabaseReference reference;
    private FirebaseUser fuser;
    private StorageReference storageReference;
    String storagePath = "Users_Profile_cover_Imgs/";
    String profileOrCoverPhoto;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUrl;
    private StorageTask uploadTask;
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_REQUEST = 200;
    String storegePermissions[];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        storegePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());

                if (user.getImageURL().equals("dafault")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getActivity()).load(user.getImageURL()).into(image_profile);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    profileOrCoverPhoto = "images";
                    pickFromGallery();
                }
            }
        });
        return view;
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(Uri imageUrl) {
        String filePAthAndName = storagePath + "" + profileOrCoverPhoto + "_" + fuser.getUid();
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();
        if (imageUrl != null) {
            final StorageReference fileReference = storageReference.child(filePAthAndName);
            fileReference.putFile(imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri=uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                HashMap<String,Object>results=new HashMap<>();
                                results.put("imageURL",downloadUri.toString());
                                reference.child(fuser.getUid()).updateChildren(results)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "Image Upload....", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "error Image Upload....", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }else {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_DENIED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storegePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStoregeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStoregeAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_PICK_GALLERY_REQUEST && requestCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUrl = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getActivity(), "Upload in preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage(imageUrl);
            }
        }
    }
}