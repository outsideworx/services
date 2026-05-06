package net.outsideworx.services.repositories.clients;

import net.outsideworx.services.models.clients.peeps.PeepsEntity;
import org.springframework.data.repository.CrudRepository;

public interface PeepsRepository extends CrudRepository<PeepsEntity, Long> {
}