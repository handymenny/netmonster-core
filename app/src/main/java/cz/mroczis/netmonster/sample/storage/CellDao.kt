package cz.mroczis.netmonster.sample.storage

import androidx.room.*

@Dao
interface CellDao {
    @Query("SELECT * FROM cell")
    fun getAll(): List<Cell>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(cells: List<Cell>)

    @Query("DELETE FROM cell")
    fun reset()

    @Query("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = \'cell\'")
    fun resetPrimaryKeyAutoIncrementValue()
}
