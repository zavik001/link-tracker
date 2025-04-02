package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.TagEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByName(String name);

    @Modifying
    @Query("DELETE FROM TagEntity t WHERE t.id NOT IN (SELECT DISTINCT ct.tag.id FROM ChatLinkTagEntity ct)")
    void cleanupUnusedTags();
}
