package com.example.michael.betterlesson.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.michael.betterlesson.R;

public class UserView extends LinearLayout{
    private ImageView userImage;
    private TextView name;
    private Color color;
    private ImageView appLogo;
    private ImageView deleteButton;//Shows up just if the user have an account

    public UserView(Context context) {
        super(context);

        userImage = new ImageView(context);
        name = new TextView(context);
        color = new Color();
        appLogo = new ImageView(context);
        deleteButton = new ImageView(context);

        this.setOrientation(HORIZONTAL);
        int margin = (int)getResources().getDimension(R.dimen.list_row_margin);
        this.setPadding(margin,margin,margin,margin);
        this.setWeightSum(1);

        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(android.R.attr.activatedBackgroundIndicator, typedValue, true);

        if (typedValue.resourceId != 0) {
            this.setBackgroundResource(typedValue.resourceId);
        } else {
            this.setBackgroundColor(typedValue.data);
        }

        userImage.setImageResource(R.mipmap.ic_account_circle_black_48dp);
        userImage.setForegroundGravity(Gravity.LEFT);
        addView(userImage);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT,0);
        param.setMargins(30,0,0,0);
        name.setTextAppearance(R.style.TextStyle);
        name.setLayoutParams(param);
        name.setGravity(Gravity.CENTER_VERTICAL);
        addView(name);

        //Set delete button and hide it
        param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,0);
        param.setMargins(0,0,50,0);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.RIGHT);
        linearLayout.setLayoutParams(param);

        param = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT,0);
        appLogo.setImageResource(R.mipmap.resize_logo);
        appLogo.setLayoutParams(param);

        deleteButton.setImageResource(R.mipmap.delete_icon_bigger);
        param.setMargins(0,0,20,0);
        deleteButton.setLayoutParams(param);
        deleteButton.setVisibility(View.GONE);

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContactFromDatabase();
            }
        });

        linearLayout.addView(deleteButton);
        linearLayout.addView(appLogo);

        addView(linearLayout);
    }

    public ImageView getUserImage() {
        return userImage;
    }

    public void setUserImage(ImageView userImage) {
        this.userImage = userImage;
    }

    public TextView getName() {
        return name;
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setDeletButtonVisibility(boolean visible){
        this.deleteButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public ImageView getAppAccountImage() {
        return appLogo;
    }

    public boolean getAppLogoVisibility(){
        if(appLogo.getVisibility()==View.VISIBLE)
            return true;
        return false;
    }

    public void setAppLogoVisibility(boolean show){
        appLogo.setVisibility(show? View.VISIBLE : View.GONE);
    }

    public void deleteContactFromDatabase(){

    }

}


