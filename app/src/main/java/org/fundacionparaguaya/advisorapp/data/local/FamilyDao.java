package org.fundacionparaguaya.advisorapp.data.local;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.fundacionparaguaya.advisorapp.models.Family;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * The access utility for retrieving families from the local database.
 */
@Dao
public interface FamilyDao {
    @Query("SELECT * FROM families WHERE is_active = 1")
    LiveData<List<Family>> queryFamilies();

    @Query("SELECT * FROM families WHERE id = :id")
    LiveData<Family> queryFamily(int id);

    /**
     * Queries for all families that only exist locally, which haven't been pushed to the
     * remote database and do not have a remote ID.
     * @return The pending families.
     */
    @Query("SELECT * FROM families WHERE remote_id IS NULL")
    List<Family> queryPendingFamilies();

    @Insert(onConflict = REPLACE)
    long insertFamily(Family family);

    @Insert(onConflict = REPLACE)
    long[] insertFamilies(Family ... families);

    @Update
    int updateFamily(Family family);

    @Delete
    int deleteFamily(Family family);

    @Query("DELETE FROM families WHERE id NOT IN (:ids)")
    void deleteFamiliesExcluding(long ... ids);
}