package org.fundacionparaguaya.advisorapp.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import org.fundacionparaguaya.advisorapp.AdvisorApplication;
import org.fundacionparaguaya.advisorapp.R;
import org.fundacionparaguaya.advisorapp.adapters.LifeMapAdapter;
import org.fundacionparaguaya.advisorapp.fragments.callbacks.LifeMapFragmentCallback;
import org.fundacionparaguaya.advisorapp.models.IndicatorOption;
import org.fundacionparaguaya.advisorapp.models.LifeMapPriority;
import org.fundacionparaguaya.advisorapp.models.Snapshot;
import org.fundacionparaguaya.advisorapp.viewcomponents.EvenBetterSpinner;
import org.fundacionparaguaya.advisorapp.viewmodels.FamilyDetailViewModel;
import org.fundacionparaguaya.advisorapp.viewmodels.InjectionViewModelFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Fragment that displays a life map for a fragment. It uses a {@link org.fundacionparaguaya.advisorapp.fragments.LifeMapFragment}
 * and provides all of the necessary callbacks.
 *
 * This fragment requires a {@link org.fundacionparaguaya.advisorapp.viewmodels.FamilyDetailViewModel} to exit
 * with it's context.
 */

public class FamilyLifeMapFragment extends Fragment implements LifeMapFragmentCallback {

    @Inject
    InjectionViewModelFactory mViewModelFactory;
    FamilyDetailViewModel mFamilyDetailViewModel;

    ArrayAdapter<Snapshot> mSpinnerAdapter;
    EvenBetterSpinner mSnapshotSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((AdvisorApplication) getActivity().getApplication())
                .getApplicationComponent()
                .inject(this);

        mFamilyDetailViewModel = ViewModelProviders
                .of(getParentFragment(), mViewModelFactory)
                .get(FamilyDetailViewModel.class);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_familylifemap, container, false);

        Fragment lifeMapFragment = getChildFragmentManager().findFragmentByTag(LifeMapFragment.class.getSimpleName());

        if(lifeMapFragment == null)
        {
            lifeMapFragment = new LifeMapFragment();

            getChildFragmentManager()
                    .beginTransaction()
                    .add( R.id.fragment_container, lifeMapFragment, LifeMapFragment.class.getSimpleName())
                    .commit();
        }

        mSnapshotSpinner = view.findViewById(R.id.spinner_familylifemap_snapshot);
        mSpinnerAdapter = new ArrayAdapter<>(this.getContext(), R.layout.item_tv_spinner);
        mSpinnerAdapter.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));

        mSnapshotSpinner.setAdapter(mSpinnerAdapter);

        mSnapshotSpinner.setOnItemClickListener((parent, view1, position, id) ->
                mFamilyDetailViewModel.setSelectedSnapshot(mSpinnerAdapter.getItem(position)));

        addViewModelObservers();

        return view;
    }


    public void removeViewModelObservers()
    {
        mFamilyDetailViewModel.getSnapshots().removeObservers(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeViewModelObservers();
    }

    public void addViewModelObservers()
    {
        mFamilyDetailViewModel.getSnapshots().observe(this, (snapshots) -> {
            mSpinnerAdapter.clear();

            if(snapshots!=null)
            {
                mSpinnerAdapter.addAll(snapshots);

                if(snapshots.size()>0)
                {
                    mSnapshotSpinner.selectFirstItem();
                    mFamilyDetailViewModel.setSelectedSnapshot(snapshots.get(0));
                }
            }
        });
    }

    @Override
    public LiveData<List<LifeMapPriority>> getPriorities() {
        return mFamilyDetailViewModel.getPriorities();
    }

    @Override
    public LiveData<Collection<IndicatorOption>> getIndicatorResponses() {
        return mFamilyDetailViewModel.getSnapshotIndicators();
    }

    @Override
    public void onLifeMapIndicatorClicked(LifeMapAdapter.LifeMapIndicatorClickedEvent e) {

    }
}
