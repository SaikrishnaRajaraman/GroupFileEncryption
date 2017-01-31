package com.zohocorp.krishna_pt1251.groupfileencryption.activites;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.zohocorp.krishna_pt1251.groupfileencryption.R;
import com.zohocorp.krishna_pt1251.groupfileencryption.fragments.ChangePasswordFragment;
import com.zohocorp.krishna_pt1251.groupfileencryption.fragments.NewFileFragment;
import com.zohocorp.krishna_pt1251.groupfileencryption.fragments.OpenFileFragment;
import com.zohocorp.krishna_pt1251.groupfileencryption.fragments.ShareFileFragment;


public class MainActivity extends AppCompatActivity  {

    private BottomNavigationView bottomNavigationView;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        bottomNavigationView=(BottomNavigationView)findViewById(R.id.bottom_nav_view);
        Bundle bundle;
        bundle=getIntent().getExtras();
        bottomNavigationView.inflateMenu(R.menu.nav_items);
        fragmentManager=getSupportFragmentManager();
        final Bundle finalBundle = bundle;

        fragment=new OpenFileFragment();
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container,fragment).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                switch (id) {
                    case R.id.nav_newFile:
                        fragment = new NewFileFragment();
                        fragment.setArguments(finalBundle);
                        break;
                    case R.id.nav_openFile:
                        fragment = new OpenFileFragment();
                        fragment.setArguments(finalBundle);
                        break;
                    case R.id.nav_shareFile:
                        fragment = new ShareFileFragment();
                        fragment.setArguments(finalBundle);
                        break;
                    case R.id.nav_changePassword:
                        fragment = new ChangePasswordFragment();
                        fragment.setArguments(finalBundle);
                        break;
                    default:
                        break;
                }

                final FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container,fragment).commit();
                return  true;


            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_logout:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


}
