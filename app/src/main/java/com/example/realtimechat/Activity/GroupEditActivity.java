package com.example.realtimechat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.realtimechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class GroupEditActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private String groupId;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    private FirebaseAuth firebaseAuth;
    private EditText group_Title, group_desc;
    private Button UpdateGroupBtn;
    private ProgressDialog progressDialog;
    private ImageView groupIv;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Group");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        groupId = getIntent().getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        group_desc = findViewById(R.id.group_desc);
        group_Title = findViewById(R.id.group_Title);
        groupIv = findViewById(R.id.groupIv);
        UpdateGroupBtn = findViewById(R.id.UpdateGroupBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        loadGroupInfo();
        //ChackUser();
        groupIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showImagePickDialog();
            }
        });
        UpdateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpdatingGroup();
            }
        });

    }

    private void startUpdatingGroup() {
        String groupTitle = group_Title.getText().toString().trim();
        String groupDescription = group_desc.getText().toString().trim();
        progressDialog.setMessage("Updatingf Group info....");
        progressDialog.show();
        if (image_uri == null) {
            //upadet group without icon
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle", groupTitle);
            hashMap.put("GroupDesc", groupDescription);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //updated...
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, "Group info updated...........", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            //upadet group without icon

            String timestamp = "" + System.currentTimeMillis();
            String filepathname = "Group_Imgs/" + "image" + "_" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filepathname);
            storageReference.putFile(image_uri)
                  .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                          if(task.isSuccessful()){
                              storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                  @Override
                                  public void onSuccess(Uri uri) {

                                      HashMap<String, Object> hashMap = new HashMap<>();
                                      hashMap.put("groupTitle", groupTitle);
                                      hashMap.put("GroupDesc", groupDescription);
                                      hashMap.put("groupIcon", ""+uri);

                                      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                      ref.child(groupId).updateChildren(hashMap)
                                              .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                  @Override
                                                  public void onSuccess(Void aVoid) {
                                                      //updated...
                                                      progressDialog.dismiss();
                                                      Toast.makeText(GroupEditActivity.this, "Group info updated...........", Toast.LENGTH_SHORT).show();

                                                  }
                                              }).addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {
                                              progressDialog.dismiss();
                                              Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                          }
                                      });
                                  }
                              });
                          }
                      }
                  }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
        //uploa


}

    private void loadGroupInfo() {
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
                            String groupIcon=""+ds.child("groupIcon").getValue();
                            group_Title.setText(groupTitle);
                            group_desc.setText(GroupDesc);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Images:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (!checkCameraPermission()) {
                                requestCameraPermission();
                            } else {
                                pickFromCamera();
                            }
                        } else {
                            if (!checkStoragePermission()) {
                                requestStoragePermission();
                            } else {
                                pickFromGallery();
                            }

                        }
                    }
                }).show();
    }

    private void pickFromGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon");
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "camera& storage Permission are Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted && storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "camera& storage Permission are Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                groupIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                image_uri = data.getData();
                groupIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}