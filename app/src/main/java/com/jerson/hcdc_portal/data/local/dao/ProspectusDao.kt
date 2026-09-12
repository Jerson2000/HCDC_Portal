package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jerson.hcdc_portal.domain.model.ProspectusEntity
import com.jerson.hcdc_portal.domain.model.ProspectusSubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspectusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProspectusMeta(meta: ProspectusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProspectusSubjects(subjects: List<ProspectusSubjectEntity>)

    @Query("SELECT * FROM prospectus_meta WHERE programCode = :programCode LIMIT 1")
    fun getProspectusMetaByProgram(programCode: String): Flow<ProspectusEntity?>

    @Query("SELECT * FROM prospectus_meta WHERE id = :prospectusId LIMIT 1")
    fun getProspectusMetaById(prospectusId: String): Flow<ProspectusEntity?>

    @Query("SELECT * FROM prospectus_subjects WHERE prospectusId = :prospectusId ORDER BY yearLevelOrder ASC, semesterOrder ASC, id ASC")
    fun getProspectusSubjects(prospectusId: String): Flow<List<ProspectusSubjectEntity>>

    @Query("DELETE FROM prospectus_subjects WHERE prospectusId = :prospectusId")
    suspend fun deleteSubjectsByProspectus(prospectusId: String)

    @Query("DELETE FROM prospectus_meta WHERE id = :prospectusId")
    suspend fun deleteProspectusMeta(prospectusId: String)

    @Transaction
    suspend fun saveFullProspectus(meta: ProspectusEntity, subjects: List<ProspectusSubjectEntity>) {
        deleteSubjectsByProspectus(meta.id)
        insertProspectusMeta(meta)
        insertProspectusSubjects(subjects)
    }
}
