package org.kickmyb.server.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

// Mostly used to have creation method, could use cascading

@Repository
public interface MProgressEventRepository extends JpaRepository<MProgressEvent, Long> { }
