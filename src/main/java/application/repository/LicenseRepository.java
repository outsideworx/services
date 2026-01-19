package application.repository;

import application.model.LicenseEntity;
import org.springframework.data.repository.CrudRepository;

public interface LicenseRepository extends CrudRepository<LicenseEntity, String> {
}