package com.msm.onlinecomplaintapp.UserActivities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.storage.UploadTask;
import com.msm.onlinecomplaintapp.Common.ImageConverter;
import com.msm.onlinecomplaintapp.Common.RandomStringBuilder;
import com.msm.onlinecomplaintapp.DepartmentAdapters.DeptListAdapter;
import com.msm.onlinecomplaintapp.GlobalApplication;
import com.msm.onlinecomplaintapp.Interfaces.OnClick;
import com.msm.onlinecomplaintapp.Interfaces.OnDataFetchListener;
import com.msm.onlinecomplaintapp.Interfaces.OnDataSFetchListener;
import com.msm.onlinecomplaintapp.Interfaces.OnDataUpdatedListener;
import com.msm.onlinecomplaintapp.Interfaces.OnPosClicked;
import com.msm.onlinecomplaintapp.Models.Complaint;
import com.msm.onlinecomplaintapp.Models.Departments;
import com.msm.onlinecomplaintapp.Models.Users;
import com.msm.onlinecomplaintapp.MyImage;
import com.msm.onlinecomplaintapp.R;
import com.msm.onlinecomplaintapp.UserActivity;
import com.msm.onlinecomplaintapp.UserAdapters.UDeptListAdapter;

public class newcomplaint extends UserActivity {

    private static final int REQUEST_CODE_CAMERA=101;
    private static final int REQUEST_CODE_GALLERY=102;

    private EditText titleedit;
    private EditText descedit;
    private ImageView compimage;
    private RadioButton publicrb;
    private RadioButton privaterb;
    private TextView depstext;
    private CheckBox cbrm;

    private ImageView camlogo;

    private List<Departments> departmentsList;
    private Users cuUser;

    private String cid;
    private int cidurif=0;

    private byte[] imageByteArray;
    private byte[] roundImageByteArray;

    private Departments selectedDepartment=null;

    private FirebaseStorage vmfbs=FirebaseStorage.getInstance();
    private StorageReference compimgfbs=vmfbs.getReference();
    private StorageReference imgStorageReference;
    private StorageReference roundImgStorageReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                Bundle extras=data.getExtras();
                Bitmap imagebitmap=(Bitmap)extras.get("data");
                Bitmap roundimagebitmap=ImageConverter.getRoundedCornerBitmap(imagebitmap,100);
                ByteArrayOutputStream imagebytes = new ByteArrayOutputStream();
                ByteArrayOutputStream roundimagebytes=new ByteArrayOutputStream();
                imagebitmap.compress(Bitmap.CompressFormat.JPEG, 100, imagebytes);
                roundimagebitmap.compress(Bitmap.CompressFormat.JPEG,100,roundimagebytes);
                imageByteArray=imagebytes.toByteArray();
                roundImageByteArray=roundimagebytes.toByteArray();
                //String path = MediaStore.Images.Media.insertImage(getContentResolver(),imagebitmap, "compimage", null);
                //Uri uri=Uri.parse(path);
                compimage.setImageBitmap(imagebitmap);
                cidurif=1;

            }
            else if (requestCode == REQUEST_CODE_GALLERY) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap imagebitmap = (BitmapFactory.decodeFile(picturePath));
                Bitmap roundimagebitmap=ImageConverter.getRoundedCornerBitmap(imagebitmap,100);
                ByteArrayOutputStream imagebytes = new ByteArrayOutputStream();
                ByteArrayOutputStream roundimagebytes=new ByteArrayOutputStream();
                imagebitmap.compress(Bitmap.CompressFormat.JPEG, 100, imagebytes);
                roundimagebitmap.compress(Bitmap.CompressFormat.JPEG,100,roundimagebytes);
                imageByteArray=imagebytes.toByteArray();
                roundImageByteArray=roundimagebytes.toByteArray();
                compimage.setImageBitmap(imagebitmap);
                cidurif=1;
            }
        }
    }
    //}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.submitbuttonmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id==android.R.id.home){
            newcomplaint.this.finish();
        }
        if (id==R.id.csubmitbutton){
            if(titleedit.getText().toString().length()>0) {
                if(descedit.getText().toString().length()>15){
                    if(selectedDepartment!=null) {
                        if (!(cbrm.isChecked() && privaterb.isChecked())) {
                            showProgress("Uploading Complaint...");
                            RandomStringBuilder randomStringBuilder = new RandomStringBuilder("cid", 7);
                            randomStringBuilder.getRandomId(new OnDataSFetchListener<String>() {
                                @Override
                                public void onDataSFetch(String s) {
                                    final Complaint complaint = new Complaint();
                                    cid=s;
                                    complaint.setCid(s);
                                    complaint.setAcm("0");
                                    complaint.setOm("0");
                                    complaint.setUid(cuUser.getUid());
                                    complaint.setTitle(titleedit.getText().toString());
                                    complaint.setDesc(descedit.getText().toString());
                                    complaint.setTime(Timestamp.now());
                                    if (publicrb.isChecked()) {
                                        complaint.setMode("public");
                                    } else {
                                        complaint.setMode("private");
                                    }
                                    complaint.setSupportno(0);
                                    if (cbrm.isChecked()) {
                                        complaint.setAmode("yes");
                                    } else {
                                        complaint.setAmode("no");
                                    }
                                    complaint.setUserName(cuUser.getFullname());
                                    complaint.setDept(selectedDepartment.getDid());
                                    if(cidurif==1) {
                                        imgStorageReference = compimgfbs.child("complaintimages/" + cid);
                                        roundImgStorageReference = compimgfbs.child("roundcomplaintimages/" + cid);
                                        imgStorageReference.putBytes(imageByteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                imgStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        complaint.setCiuri(uri.toString());
                                                        roundImgStorageReference.putBytes(roundImageByteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                roundImgStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {
                                                                        complaint.setCriuri(uri.toString());
                                                                        GlobalApplication.databaseHelper.updateComplaint(complaint, new OnDataUpdatedListener() {
                                                                            @Override
                                                                            public void onDataUploaded(boolean success) {
                                                                                hideProgress();
                                                                                if(success) {
                                                                                    Toast.makeText(newcomplaint.this, "Complaint Registered", Toast.LENGTH_LONG).show();
                                                                                    newcomplaint.this.finish();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    else {
                                        GlobalApplication.databaseHelper.updateComplaint(complaint, new OnDataUpdatedListener() {
                                            @Override
                                            public void onDataUploaded(boolean success) {
                                                hideProgress();
                                                if (success) {
                                                    Toast.makeText(newcomplaint.this, "Complaint Registered", Toast.LENGTH_LONG).show();
                                                    newcomplaint.this.finish();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                    else {
                        depstext.setError("A department must be selected");
                    }
                }
                else {
                    descedit.setError("Description must have at least 15 charachters");
                }
            }
            else{
                titleedit.setError("Title must not be empty");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcomplaint);
        Initialize();
    }

    private void Initialize(){

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showProgress("Loading...");

        titleedit=findViewById(R.id.titleedit);
        descedit=findViewById(R.id.descedit);
        compimage=findViewById(R.id.compimage);
        publicrb=findViewById(R.id.publicrb);
        privaterb=findViewById(R.id.privaterb);
        depstext=findViewById(R.id.depstext);
        cbrm=findViewById(R.id.cbrm);
        camlogo=findViewById(R.id.camlogo);

        privaterb.setChecked(true);


        camlogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(newcomplaint.this);
                builder.setTitle("Add Photo!");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo"))
                        {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CODE_CAMERA);
                        }
                        else if (options[item].equals("Choose from Gallery"))
                        {
                            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQUEST_CODE_GALLERY);
                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                if (ContextCompat.checkSelfPermission(newcomplaint.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(newcomplaint.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(newcomplaint.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                }
                else{
                    builder.show();
                }
            }
        });


        depstext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(newcomplaint.this);
                View view1 = bottomSheetDialog.getLayoutInflater().inflate(R.layout.deplistlayout, null);
                bottomSheetDialog.setContentView(view1);
                ListView listView = view1.findViewById(R.id.depsellist);
                TextView dlladmintext = view1.findViewById(R.id.dlldmintext);
                dlladmintext.setVisibility(View.GONE);
                int temppos = -1;
                if(selectedDepartment!=null) {
                    for (int i = 0; i < departmentsList.size(); i++) {
                        if (selectedDepartment.getDid().contains(departmentsList.get(i).getDid()))
                            temppos = i;
                    }
                }
                UDeptListAdapter uDeptListAdapter=new UDeptListAdapter(departmentsList,temppos,newcomplaint.this);
                uDeptListAdapter.dissmiss(new OnPosClicked() {
                    @Override
                    public void onSelected(int pos) {
                        selectedDepartment=departmentsList.get(pos);
                        bottomSheetDialog.dismiss();
                        depstext.setText(selectedDepartment.getName());
                    }
                });
                bottomSheetDialog.show();
                listView.setAdapter(uDeptListAdapter);
            }
        });

        GlobalApplication.databaseHelper.fetchUserData(getCurrentUserId(), new OnDataSFetchListener<Users>() {
            @Override
            public void onDataSFetch(Users users) {
                cuUser=users;
                GlobalApplication.databaseHelper.getDepartmentsList(new OnDataFetchListener<Departments>() {
                    @Override
                    public void onDataFetched(List<Departments> departments) {
                        departmentsList=departments;
                        hideProgress();
                    }
                });
            }
        });

    }

}
