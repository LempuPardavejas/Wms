package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeriesRepository extends JpaRepository<Series, UUID> {

    Optional<Series> findByCode(String code);

    List<Series> findByIsActiveTrue();

    List<Series> findBySeriesType(Series.SeriesType seriesType);

    boolean existsByCode(String code);
}
