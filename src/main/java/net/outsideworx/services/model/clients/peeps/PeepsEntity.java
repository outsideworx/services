package net.outsideworx.services.model.clients.peeps;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "PEEPS")
public final class PeepsEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Transient
    private Boolean delete;
    private String link;
    private String title;
}