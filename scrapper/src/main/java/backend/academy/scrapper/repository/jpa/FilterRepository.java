package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.FilterEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends JpaRepository<FilterEntity, Long> {
    Optional<FilterEntity> findByValue(String value);
}
