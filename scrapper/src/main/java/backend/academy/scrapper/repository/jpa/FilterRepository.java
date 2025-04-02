package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.FilterEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends JpaRepository<FilterEntity, Long> {
    Optional<FilterEntity> findByValue(String value);

    @Modifying
    @Query("DELETE FROM FilterEntity f WHERE f.id NOT IN (SELECT DISTINCT cf.filter.id FROM ChatLinkFilterEntity cf)")
    void cleanupUnusedFilters();
}
