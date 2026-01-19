package application.repository.clients;

import application.model.clients.soup.SoupEntity;
import application.model.clients.soup.mapping.SoupImage;
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
            SELECT id, category, description, image, thumbnail
                    FROM SOUP
                    WHERE category = :category
            """, nativeQuery = true)
    List<SoupEntity> getThumbnailsByCategory(String category);

    @Cacheable(value = "soupItems", key = "#category + #offset")
    @Query(value = """
            SELECT id, category, description, image, thumbnail
                    FROM SOUP
                    WHERE category = :category
                    ORDER BY id
                    LIMIT 9 OFFSET :offset
            """, nativeQuery = true)
    List<SoupImage> getImagesByCategoryAndOffset(String category, int offset);

    @Modifying
    @Query(value = """
            UPDATE SOUP SET
                    category = :#{#item.category},
                    description = :#{#item.description},
                    image = COALESCE(:#{#item.image}, image),
                    thumbnail = COALESCE(:#{#item.thumbnail}, thumbnail)
                    WHERE id = :#{#item.id}
            """, nativeQuery = true)
    void update(SoupEntity item);
}