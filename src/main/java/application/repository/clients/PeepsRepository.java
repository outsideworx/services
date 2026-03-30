package application.repository.clients;

import application.model.clients.peeps.PeepsEntity;
import org.springframework.data.repository.CrudRepository;

public interface PeepsRepository extends CrudRepository<PeepsEntity, Long> {
}