package ru.eliseevtech.entryservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.eliseevtech.entryservice.entity.Entry;

import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    Optional<Entry> findFirstByPositionLessThanOrderByPositionDesc(Long position);

    Optional<Entry> findFirstByPositionGreaterThanOrderByPositionAsc(Long position);

    Optional<Entry> findByPosition(Long position);

    @Modifying
    @Query(value = """
            UPDATE entry SET position = :tmp WHERE id = :aId;
            UPDATE entry SET position = :posA WHERE id = :bId;
            UPDATE entry SET position = :posB WHERE id = :aId;
            """, nativeQuery = true)
    void swapWithBuffer(long aId, long bId, long posA, long posB, long tmp);
}
