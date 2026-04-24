package net.outsideworx.services.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CALLBACK")
public final class CallbackEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String address;
    private Boolean processed;
    private String product;
    private String recipient;
}