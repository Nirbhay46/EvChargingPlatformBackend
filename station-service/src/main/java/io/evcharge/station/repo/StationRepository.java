package io.evcharge.station.repo;

import io.evcharge.station.domain.Station;
import io.evcharge.station.domain.StationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StationRepository extends JpaRepository<Station, Long> {

    Page<Station> findByStatus(StationStatus status, Pageable pageable);

    Page<Station> findByCityIgnoreCaseAndStatus(String city, StationStatus status, Pageable pageable);

    @Query("""
       SELECT s FROM Station s
        WHERE (:city IS NULL OR LOWER(s.city) = LOWER(:city))
          AND (:status IS NULL OR s.status = :status)
       """)
    Page<Station> search(String city, StationStatus status, Pageable pageable);
}
