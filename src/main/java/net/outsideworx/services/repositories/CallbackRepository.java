package net.outsideworx.services.repositories;

import net.outsideworx.services.models.CallbackEntity;
import org.springframework.data.repository.CrudRepository;

public interface CallbackRepository extends CrudRepository<CallbackEntity, Long> {
}