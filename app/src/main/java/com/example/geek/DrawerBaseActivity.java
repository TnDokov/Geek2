package com.example.geek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.navigation.NavigationView;

public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout dl;

    @Override
    public void setContentView(View view) {
        dl= (DrawerLayout)getLayoutInflater().inflate(R.layout.activity_drawer_base_activity,null);
        FrameLayout fl = dl.findViewById(R.id.activityContainer);
        fl.addView(view);
        super.setContentView(dl);

        Toolbar tl = dl.findViewById(R.id.toolbar);
        setSupportActionBar(tl);

        NavigationView nv = dl.findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle tg = new ActionBarDrawerToggle(this,dl,tl,R.string.menu_drawer_open,R.string.menu_drawer_close);
        dl.addDrawerListener(tg);
        tg.syncState();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        dl.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.nav_dashboard:
                Intent i = new Intent(this,Dashboard.class);
                startActivity(i);
                break;
            case R.id.nav_faq:
                String url_faq = "http://www.digipas.com/support/faq.php";
                Intent faq = new Intent(Intent.ACTION_VIEW);
                faq.setData(Uri.parse(url_faq));
                startActivity(faq);
                break;

            case R.id.logout:
                Intent il = new Intent(this,Login.class);
                startActivity(il);
                break;
        }



        return false;
    }

    protected void allocateActivtityTitle(String strintitle){
        if(getSupportActionBar()!= null){
            getSupportActionBar().setTitle(strintitle);
        }
    }
}