package application.repository.clients;

import application.model.clients.soup.SoupEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SoupRepository extends CrudRepository<SoupEntity, Long> {
    @Cacheable(value = "soupItems", key = "#category")
    @Query(value = """
            SELECT id, category, description, hash, image, thumbnail, link
                    FROM SOUP
                    WHERE category = :category
                    ORDER BY id
            """, nativeQuery = true)
    List<SoupEntity> get(String category);

    @Cacheable(value = "soupItems", key = "#category + #offset")
    @Query(value = """
            SELECT id, category, description, hash, image, thumbnail, link
                    FROM SOUP
                    WHERE category = :category
                    ORDER BY id
                    LIMIT 9 OFFSET :offset
            """, nativeQuery = true)
    List<SoupEntity> get(String category, int offset);

    @Modifying
    @Query(value = """
            UPDATE SOUP SET
                    category = :#{#item.category},
                    description = :#{#item.description},
                    hash = :#{#item.hash},
                    image = COALESCE(:#{#item.image}, image),
                    thumbnail = COALESCE(:#{#item.thumbnail}, thumbnail),
                    link = COALESCE(:#{#item.link}, link)
                    WHERE id = :#{#item.id}
            """, nativeQuery = true)
    void update(SoupEntity item);
}