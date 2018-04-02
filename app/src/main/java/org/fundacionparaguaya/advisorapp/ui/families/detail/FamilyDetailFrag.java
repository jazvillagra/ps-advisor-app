package org.fundacionparaguaya.advisorapp.ui.families.detail;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.instabug.library.Instabug;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.fundacionparaguaya.advisorapp.AdvisorApplication;
import org.fundacionparaguaya.advisorapp.R;
import org.fundacionparaguaya.advisorapp.ui.survey.SurveyActivity;
import org.fundacionparaguaya.advisorapp.ui.common.LifeMapAdapter;
import org.fundacionparaguaya.advisorapp.ui.common.LifeMapFragmentCallback;
import org.fundacionparaguaya.advisorapp.data.model.Family;
import org.fundacionparaguaya.advisorapp.data.model.IndicatorOption;
import org.fundacionparaguaya.advisorapp.data.model.LifeMapPriority;
import org.fundacionparaguaya.advisorapp.ui.base.AbstractStackedFrag;
import org.fundacionparaguaya.advisorapp.util.MixpanelHelper;
import org.fundacionparaguaya.advisorapp.injection.InjectionViewModelFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;


public class FamilyDetailFrag extends AbstractStackedFrag implements Observer<Family>, LifeMapFragmentCallback {

    private static String SELECTED_FAMILY_KEY = "SELECTED_FAMILY";

    private TextView mFamilyName;
    private TextView mAddress;
    private TextView mLocation;
    private SimpleDraweeView mFamilyImage;
    private TextView mPhoneNumber;

    int mFamilyId = -1;

    @Inject
    InjectionViewModelFactory mViewModelFactory;
    FamilyDetailViewModel mFamilyInformationViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){

        ((AdvisorApplication) getActivity().getApplication())
                .getApplicationComponent()
                .inject(this);

        mFamilyInformationViewModel = ViewModelProviders
               .of(this, mViewModelFactory)
                .get(FamilyDetailViewModel.class);

        if (getArguments() != null) {
            Bundle args = getArguments();
            mFamilyId = args.getInt(SELECTED_FAMILY_KEY);
            mFamilyInformationViewModel.setFamily(mFamilyId);
            //wait for family to load here
        }
        else
        {
            throw new IllegalArgumentException(FamilyDetailFrag.class.getName() + " requires the family id to be displayed" +
                    "to be passed in as an argument.");
        }

        mFamilyInformationViewModel.getSnapshotIndicators().observe(this, indicatorOptions -> {
            Log.d("", "Updated");
        });

        super.onCreate(savedInstanceState); //must be called after the view model is instantiated (because the
                                            //super oncreate will create the child fragments
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_familydetail, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFamilyName = view.findViewById(R.id.family_view_name);
        mPhoneNumber = view.findViewById(R.id.familyview_phone);
       // mAddress = view.findViewById(R.id.familydetail_location_content);
     //   mLocation = view.findViewById(R.id.description_content);
        mFamilyImage = view.findViewById(R.id.family_image_2);

        ViewPager viewPager = view.findViewById(R.id.pager_familydetail);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new FamilyLifeMapFragment(), getResources().getString(R.string.life_map_title));
        adapter.addFragment(new FamilyPrioritiesFrag(), getResources().getString(R.string.priorities));

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs_familydetail);
        tabLayout.setupWithViewPager(viewPager);

        view.findViewById(R.id.btn_familydetail_newsnapshot).setOnClickListener((v)-> takeSnapshot());

        try{
            //observer is added onViewCreated so the LiveData will renotify the observers when the view is
            // destoryed/recreated
            mFamilyInformationViewModel.getCurrentFamily().observe(this, this);
        }
        catch (IllegalStateException e)
        {
            Log.e(FamilyDetailFrag.class.getName(), e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mFamilyInformationViewModel.getCurrentFamily().removeObserver(this);
    }

    @Override
    public void onChanged(@Nullable Family family) {
        mFamilyName.setText(family.getName());

        /*
        mAddress.setText(family.getAddress());
        mLocation.setText((CharSequence) family.getLocation());


        if (family.getMember() != null){
            mAddress.setText(family.getAddress());
        } else {
            mAddress.setText(getString(R.string.familydetails_locationdefault));
        }  */

        if (family.getMember() != null) {
            mPhoneNumber.setText(family.getMember().getPhoneNumber());
        } else {
            mPhoneNumber.setText(getText(R.string.familydetails_phonenumberdefault));
        }

        if (mFamilyInformationViewModel.hasImageUri()) {
            Uri uri = mFamilyInformationViewModel.getImageUri();
            mFamilyImage.setImageURI(uri);
        }
    }

    public static FamilyDetailFrag build(int familyId)
    {
        Bundle args = new Bundle();
        args.putInt(SELECTED_FAMILY_KEY, familyId);
        FamilyDetailFrag f = new FamilyDetailFrag();
        f.setArguments(args);

        return f;
    }

    public void takeSnapshot() {
        if (mFamilyInformationViewModel.getCurrentFamily().getValue().getMember() == null) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.familydetail_nullmember_title))
                    .setContentText(getString(R.string.familydetail_nullmember_content))
                    .setConfirmText(getString(R.string.all_okay))
                    .setConfirmClickListener(Dialog::dismiss).show();

            Instabug.reportException(new Exception(getString(R.string.familydetail_nullmember_content)));
        }
        else {
            MixpanelHelper.SurveyEvents.startResurvey(getContext());

            Intent surveyIntent = SurveyActivity.build(getContext(),
                    mFamilyInformationViewModel.getCurrentFamily().getValue());

            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();

            startActivity(surveyIntent, bundle);
        }
    }

    @Override
    public LiveData<List<LifeMapPriority>> getPriorities() {
        return mFamilyInformationViewModel.getPriorities();
    }

    @Override
    public LiveData<Collection<IndicatorOption>> getIndicatorResponses() {
        return mFamilyInformationViewModel.getSnapshotIndicators();
    }

    @Override
    public void onLifeMapIndicatorClicked(LifeMapAdapter.LifeMapIndicatorClickedEvent e) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}