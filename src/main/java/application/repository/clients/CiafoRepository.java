package application.repository.clients;

import application.model.clients.ciafo.mapping.CiafoFirstImage;
import application.model.clients.ciafo.mapping.CiafoImages;
import application.model.clients.ciafo.CiafoEntity;
import application.model.clients.ciafo.mapping.CiafoThumbnails;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CiafoRepository extends CrudRepository<CiafoEntity, Long> {
    @Cacheable(value = "ciafoItems", key = "#category")
    @Query(value = """
            SELECT id, category, description, hash, thumbnail1, thumbnail2, thumbnail3, thumbnail4
                    FROM CIAFO
                    WHERE category = :category
            """, nativeQuery = true)
    List<CiafoThumbnails> getThumbnailsByCategory(String category);

    @Cacheable(value = "ciafoItems", key = "#id")
    @Query(value = """
            SELECT id, category, description, hash, image1, image2, image3, image4
                    FROM CIAFO
                    WHERE id = :id
            """, nativeQuery = true)
    CiafoImages getImagesById(Long id);

    @Cacheable(value = "ciafoItems", key = "#category + #offset")
    @Query(value = """
            SELECT id, category, description, hash, image1
                    FROM CIAFO
                    WHERE category = :category
                    ORDER BY id
                    LIMIT 6 OFFSET :offset
            """, nativeQuery = true)
    List<CiafoFirstImage> getFirstImagesByCategoryAndOffset(String category, int offset);

    @Modifying
    @Query(value = """
            UPDATE CIAFO SET
                    category = :#{#item.category},
                    description = :#{#item.description},
                    hash = :#{#item.hash},
                    image1 = COALESCE(:#{#item.image1}, image1),
                    image2 = COALESCE(:#{#item.image2}, image2),
                    image3 = COALESCE(:#{#item.image3}, image3),
                    image4 = COALESCE(:#{#item.image4}, image4),
                    thumbnail1 = COALESCE(:#{#item.thumbnail1}, thumbnail1),
                    thumbnail2 = COALESCE(:#{#item.thumbnail2}, thumbnail2),
                    thumbnail3 = COALESCE(:#{#item.thumbnail3}, thumbnail3),
                    thumbnail4 = COALESCE(:#{#item.thumbnail4}, thumbnail4)
                    WHERE id = :#{#item.id}
            """, nativeQuery = true)
    void update(CiafoEntity item);
}