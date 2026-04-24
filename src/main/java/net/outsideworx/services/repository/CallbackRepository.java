package net.outsideworx.services.repository;

import net.outsideworx.services.model.CallbackEntity;
import org.springframework.data.repository.CrudRepository;

public interface CallbackRepository extends CrudRepository<CallbackEntity, Long> {
}