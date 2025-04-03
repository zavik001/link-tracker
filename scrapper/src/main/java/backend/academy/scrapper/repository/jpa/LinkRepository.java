package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.LinkEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByUrl(String url);

    @Modifying
    @Query("DELETE FROM LinkEntity l WHERE l.id NOT IN (SELECT DISTINCT cl.link.id FROM ChatLinkEntity cl)")
    void cleanupUnusedLinks();
}
