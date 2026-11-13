package net.outsideworx.services.repositories.clients;

import net.outsideworx.services.models.clients.worx.KeyValueEntity;
import org.springframework.data.repository.CrudRepository;

public interface KeyValueRepository extends CrudRepository<KeyValueEntity, Long> {
}
