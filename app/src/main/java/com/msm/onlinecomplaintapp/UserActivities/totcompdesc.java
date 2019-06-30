package com.msm.onlinecomplaintapp.UserActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.msm.onlinecomplaintapp.Common.ImageConverter;
import com.msm.onlinecomplaintapp.Interfaces.CDOnClick;
import com.msm.onlinecomplaintapp.Interfaces.OnClick;
import com.msm.onlinecomplaintapp.R;
import com.msm.onlinecomplaintapp.UserActivity;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class totcompdesc extends UserActivity {

    private LinearLayout tcdimglinear;
    private TextView tcdheadingtext;
    private TextView timeauthtext;
    private TextView tcddesctext;
    private TextView tcdsnt;
    private ImageView tcdimg;
    private TextView authtext;
    private CircleImageView authimg;
    private TextView statustext;
    private LinearLayout tcdstatusmsglinear;
    private TextView tcdstatusmsg;
    private ToggleButton tcdsupportbutton;

    private HashMap<String,Object> cuComplaintmap;
    private Boolean sbe;
    private Boolean sbp;
    private String uid;
    private int supportno;
    private Boolean tempbool;

    private FirebaseFirestore database=FirebaseFirestore.getInstance();

    public static final String COMPLAINT_DB_KEY="Complaints";
    public static final String USERS_DB_KEY = "Users";
    public static final String SUPP_DB_KEY = "Support";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totcompdesc);

        tcdimglinear = findViewById(R.id.tcdimglinear);
        tcdimg = findViewById(R.id.tcdimg);
        tcddesctext = findViewById(R.id.tcddesctext);
        tcdheadingtext = findViewById(R.id.tcdheadingtext);
        tcdsnt = findViewById(R.id.tcdsnt);
        timeauthtext = findViewById(R.id.timeauthtext);
        authimg = findViewById(R.id.authimage);
        authtext = findViewById(R.id.authtext);
        statustext = findViewById(R.id.statustext);
        tcdstatusmsglinear = findViewById(R.id.tcdmsglinear);
        tcdstatusmsg = findViewById(R.id.tcdmsg);
        tcdsupportbutton=findViewById(R.id.tcdsupportbutton);

        cuComplaintmap = (HashMap<String, Object>) getIntent().getSerializableExtra("cuComplaint");
        sbe=getIntent().getBooleanExtra("sbe",false);
        sbp=getIntent().getBooleanExtra("sbp",false);


        tcdheadingtext.setText(cuComplaintmap.get("title").toString());
        tcdheadingtext.setAllCaps(true);

        uid=getCurrentUserId();

        if (cuComplaintmap.get("ciuri") != null) {
            if (cuComplaintmap.get("ciuri").toString().length() != 0) {
                tcdimglinear.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(cuComplaintmap.get("ciuri").toString()).into(tcdimg);
            } else {
                tcdimg.setVisibility(View.GONE);
                tcdimglinear.setVisibility(View.GONE);
            }
        } else {
            tcdimg.setVisibility(View.GONE);
            tcdimglinear.setVisibility(View.GONE);
        }
        tcddesctext.setText(cuComplaintmap.get("desc").toString());
        supportno=(int)cuComplaintmap.get("supportno");
        tcdsnt.setText("Support:" + String.valueOf(supportno));
        SimpleDateFormat sfd = new SimpleDateFormat("dd MMM yyyy | hh:mm a");
        Timestamp timestamp = (Timestamp) cuComplaintmap.get("time");
        timeauthtext.setText(sfd.format(new Date(timestamp.getSeconds() * 1000L)));
        if (cuComplaintmap.get("amode").toString().equals("yes")) {
            authtext.setText("Ananymous");
        } else {
            authtext.setText(cuComplaintmap.get("userName").toString());
        }
        if (cuComplaintmap.get("acm").equals("0")) {
            statustext.setText("Registered");
            statustext.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(R.drawable.ic_registered_grey_18dp), null, null, null);
        } else if (cuComplaintmap.get("acm").equals("1")) {
            statustext.setText("Under Watch");
            statustext.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(R.drawable.ic_underwatch_18dp), null, null, null);
        } else if (cuComplaintmap.get("acm").equals("2")) {
            statustext.setText("Resolved");
            statustext.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(R.drawable.ic_solved_18dp), null, null, null);
        } else {
            statustext.setText("Ignored");
            statustext.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(R.drawable.ic_ignored_18dp), null, null, null);
        }
        if (cuComplaintmap.get("statement") != null) {
            if (!cuComplaintmap.get("statement").toString().equals("")) {
                tcdstatusmsglinear.setVisibility(View.VISIBLE);
                tcdstatusmsg.setText(cuComplaintmap.get("statement").toString());
            }
        }
        if(sbe) {
            tempbool=sbp;
            tcdsupportbutton.setVisibility(View.VISIBLE);
            tcdsupportbutton.setChecked(sbp);
            tcdsupportbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tcdsupportbutton.isChecked()) {
                        tempbool=true;
                        supportno++;
                        tcdsnt.setText("Support:" + String.valueOf(supportno));
                        final Map<String, Object> map = new HashMap<>();
                        map.put("time", Timestamp.now());
                        database.collection(USERS_DB_KEY).document(uid).collection(SUPP_DB_KEY).document(cuComplaintmap.get("cid").toString()).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.collection(COMPLAINT_DB_KEY).document(cuComplaintmap.get("cid").toString()).update("supportno", supportno).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        tcdsnt.setText("Support:" + String.valueOf(supportno));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        supportno--;
                                        tcdsnt.setText("Support:" + String.valueOf(supportno));
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                supportno--;
                                tcdsnt.setText("Support:" + String.valueOf(supportno));
                            }
                        });

                    } else {
                        tempbool=false;
                        supportno--;
                        tcdsnt.setText("Support:" + String.valueOf(supportno));
                        database.collection(USERS_DB_KEY).document(uid).collection(SUPP_DB_KEY).document(cuComplaintmap.get("cid").toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.collection(COMPLAINT_DB_KEY).document(cuComplaintmap.get("cid").toString()).update("supportno", supportno).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        tcdsnt.setText("Support:" + String.valueOf(supportno));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        supportno++;
                                        tcdsnt.setText("Support:" + String.valueOf(supportno));
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                supportno++;
                                tcdsnt.setText("Support:" + String.valueOf(supportno));
                            }
                        });
                    }
                }
            });
        }
    }
}
