package org.fundacionparaguaya.adviserplatform.ui.survey.resume;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.fundacionparaguaya.adviserplatform.data.model.Family;
import org.fundacionparaguaya.adviserplatform.data.model.Snapshot;
import org.fundacionparaguaya.adviserplatform.data.model.Survey;
import org.fundacionparaguaya.adviserplatform.data.repositories.FamilyRepository;
import org.fundacionparaguaya.adviserplatform.data.repositories.SurveyRepository;

/**
 * The view model holding information about a pending snapshot.
 */

public class PendingSnapshotViewModel extends ViewModel {
    private SurveyRepository mSurveyRepository;
    private FamilyRepository mFamilyRepository;

    private MutableLiveData<Snapshot> mSnapshot;
    private MediatorLiveData<Survey> mSurvey;
    private LiveData<Survey> mSurveySource;
    private MediatorLiveData<Family> mFamily;
    private LiveData<Family> mFamilySource;


    public PendingSnapshotViewModel(SurveyRepository surveyRepository,
                                    FamilyRepository familyRepository) {
        mSurveyRepository = surveyRepository;
        mFamilyRepository = familyRepository;

        mSnapshot = new MutableLiveData<>();
        mSurvey = new MediatorLiveData<>();
        mFamily = new MediatorLiveData<>();
    }

    /**
     * Sets the snapshot that is in progress.
     */
    public void setSnapshot(@NonNull Snapshot snapshot) {
        mSnapshot.setValue(snapshot);

        LiveData<Survey> newSurveySource = mSurveyRepository.getSurvey(snapshot.getSurveyId());
        replaceSource(mSurvey, mSurveySource, newSurveySource);
        mSurveySource = newSurveySource;

        // get the family if this snapshot has one, or replace with a fake source otherwise
        Integer familyId = snapshot.getFamilyId();
        LiveData<Family> newFamilySource;
        if (familyId != null) {
            newFamilySource = mFamilyRepository.getFamily(familyId);
        } else {
            newFamilySource = mFamilyRepository.getFamily(-1);
        }
        replaceSource(mFamily, mFamilySource, newFamilySource);
        mFamilySource = newFamilySource;
    }

    public @Nullable Snapshot getSnapshot() {
        return mSnapshot.getValue();
    }

    public LiveData<Snapshot> snapshot() {
        return mSnapshot;
    }

    public Survey getSurvey() {
        return mSurvey.getValue();
    }

    public LiveData<Survey> survey() {
        return mSurvey;
    }

    public Family getFamily() {
        return mFamily.getValue();
    }

    public LiveData<Family> family() {
        return mFamily;
    }

    private <T> void  replaceSource(@NonNull MediatorLiveData<T> mediator,
                                    LiveData<T> oldSource,
                                    LiveData<T> newSource) {
        if (oldSource != null) {
            mediator.removeSource(oldSource);
        }
        if (newSource != null) {
            mediator.addSource(newSource, mediator::setValue);
        }
    }

}
