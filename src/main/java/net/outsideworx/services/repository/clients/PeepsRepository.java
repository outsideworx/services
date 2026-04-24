package net.outsideworx.services.repository.clients;

import net.outsideworx.services.model.clients.peeps.PeepsEntity;
import org.springframework.data.repository.CrudRepository;

public interface PeepsRepository extends CrudRepository<PeepsEntity, Long> {
}