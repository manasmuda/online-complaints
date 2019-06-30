package com.msm.onlinecomplaintapp.UserMenus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.msm.onlinecomplaintapp.Common.BitMapLoader;
import com.msm.onlinecomplaintapp.Common.ConfirmationDialog;
import com.msm.onlinecomplaintapp.Common.ImageConverter;
import com.msm.onlinecomplaintapp.DepartmentActivities.depttotcompdec;
import com.msm.onlinecomplaintapp.GlobalApplication;
import com.msm.onlinecomplaintapp.Interfaces.CDOnClick;
import com.msm.onlinecomplaintapp.Interfaces.OnDataUpdatedListener;
import com.msm.onlinecomplaintapp.Interfaces.OnLoad;
import com.msm.onlinecomplaintapp.Models.Complaint;
import com.msm.onlinecomplaintapp.R;
import com.msm.onlinecomplaintapp.UserActivities.mycomplaints;
import com.msm.onlinecomplaintapp.UserActivities.totcompdesc;
import com.msm.onlinecomplaintapp.UserActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserOpenMenu extends BottomSheetDialog {

    private Context mContext;
    private Button uomviewcompbutton;
    private Button uomchangemodebutton;
    private Button uommsglogsbutton;
    private Button uomdeletebutton;
    private Button uomdonebutton;
    private CircleImageView uomcompimage;
    private Complaint cuComplaint;
    private View mPassedView;
    private Boolean tsbp;

    public UserOpenMenu(Context context,Complaint complaint,View passedView,Boolean tsbp) {
        super(context);
        mContext=context;
        cuComplaint=complaint;
        mPassedView=passedView;
        this.tsbp=tsbp;
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        uomchangemodebutton=view.findViewById(R.id.uomchangemodebutton);
        uomviewcompbutton=view.findViewById(R.id.uomviewcompbutton);
        uommsglogsbutton=view.findViewById(R.id.uommsglogsbutton);
        uomdeletebutton=view.findViewById(R.id.uomdeletebutton);
        uomdonebutton=view.findViewById(R.id.uomdonebutton);
        uomcompimage=view.findViewById(R.id.uomimage);



        if(cuComplaint.getCriuri()==null || cuComplaint.getCriuri().equals("")){
            uomcompimage.setVisibility(View.GONE);
        }
        else {
            Glide.with(mContext.getApplicationContext()).load(cuComplaint.getCriuri()).placeholder(R.drawable.ic_launcher_foreground).into(uomcompimage);
        }

        uomviewcompbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent=new Intent();
                intent.setClass(mContext, totcompdesc.class);
                intent.putExtra("cuComplaint",cuComplaint.toHashMap());
                intent.putExtra("sbe",true);
                intent.putExtra("sbp",tsbp);
                ActivityOptionsCompat activityOptionsCompat=ActivityOptionsCompat.makeSceneTransitionAnimation((mycomplaints)mContext,mPassedView,"trans2");
                ((mycomplaints) mContext).startActivityForResult(intent,6,activityOptionsCompat.toBundle());
            }
        });

        uomchangemodebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                final Dialog dialog=new Dialog(mContext);
                View view1=dialog.getLayoutInflater().inflate(R.layout.changemodelayout,null);
                dialog.setContentView(view1);
                dialog.getWindow().getAttributes().windowAnimations=R.style.DialogSlide;
                final Button cmapplybutton=view1.findViewById(R.id.cmapplybutton);
                final RadioButton cmpublicrb=view1.findViewById(R.id.cmpublicrb);
                final RadioButton cmprivaterb=view1.findViewById(R.id.cmprivaterb);
                final RadioButton cmanarb=view1.findViewById(R.id.cmanarb);
                final RadioButton cmnanarb=view1.findViewById(R.id.cmnanarb);
                final RadioGroup cmarg=view1.findViewById(R.id.cmarg);
                final RadioGroup cmprg=view1.findViewById(R.id.cmprg);
                if(cuComplaint.getMode().equals("public")){
                    cmpublicrb.setChecked(true);
                }
                else {
                    cmprivaterb.setChecked(true);
                }
                if(cuComplaint.getAmode().equals("yes")){
                    cmanarb.setChecked(true);
                }
                else {
                    cmnanarb.setChecked(true);
                }
                cmapplybutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ((UserActivity)mContext).showProgress("Changing Mode...");
                        if(cmpublicrb.isChecked()){
                            cuComplaint.setMode("public");
                        }
                        else {
                            cuComplaint.setMode("private");
                        }
                        if(cmanarb.isChecked()){
                            cuComplaint.setAmode("yes");
                        }
                        else {
                            cuComplaint.setAmode("no");
                        }
                        GlobalApplication.databaseHelper.updateComplaint(cuComplaint, new OnDataUpdatedListener() {
                            @Override
                            public void onDataUploaded(boolean success) {
                                ((UserActivity)mContext).hideProgress();
                            }
                        });
                    }
                });
                dialog.show();
            }
        });

        uomdeletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                final ConfirmationDialog confirmationDialog=new ConfirmationDialog(mContext,"Delete");
                View view1=confirmationDialog.getLayoutInflater().inflate(R.layout.confirmationdialoglayout,null);
                confirmationDialog.setContentView(view1);
                confirmationDialog.show();
                confirmationDialog.choicelistener(new CDOnClick() {
                    @Override
                    public void OnButtonClicked(boolean choice) {
                        confirmationDialog.dismiss();
                        if(choice){
                            ((UserActivity)mContext).showProgress("Deleting...");
                            GlobalApplication.databaseHelper.deleteComplaint(cuComplaint.getCid(), new OnDataUpdatedListener() {
                                @Override
                                public void onDataUploaded(boolean success) {
                                    ((UserActivity)mContext).hideProgress();
                                }
                            });
                        }
                    }
                });
            }
        });

        uomdonebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}
