package org.fundacionparaguaya.advisorapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.fundacionparaguaya.advisorapp.R;
import org.fundacionparaguaya.advisorapp.fragments.AllFamiliesFragment;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity
{
    private static final String ALL_FAMILIES_FRAG = "ALL_FAMILIES_FRAG";
    @Inject
    AllFamiliesFragment mAllFamiliesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();

        AllFamiliesFragment allFamiliesFragment =
                (AllFamiliesFragment) manager.findFragmentByTag(ALL_FAMILIES_FRAG);

        if (allFamiliesFragment == null)
            allFamiliesFragment = new AllFamiliesFragment();

        if (findViewById(R.id.fragment_container) != null) {

            // Add the fragment to the 'fragment_container' FrameLayout
            manager.beginTransaction()
                    .add(R.id.fragment_container, allFamiliesFragment).commit();
        }
    }
}
