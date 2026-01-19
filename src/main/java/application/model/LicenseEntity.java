package application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "LICENSES")
public final class LicenseEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String text;
    @ManyToMany
    private List<UserEntity> users;
}
