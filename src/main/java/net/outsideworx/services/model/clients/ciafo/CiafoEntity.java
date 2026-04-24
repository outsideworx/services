package net.outsideworx.services.model.clients.ciafo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "CIAFO")
public final class CiafoEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String category;
    @Transient
    private Boolean delete;
    private String description;
    private String hash;
    @Column(columnDefinition = "TEXT")
    private String image1;
    @Column(columnDefinition = "TEXT")
    private String image2;
    @Column(columnDefinition = "TEXT")
    private String image3;
    @Column(columnDefinition = "TEXT")
    private String image4;
    @Column(columnDefinition = "TEXT")
    private String thumbnail1;
    @Column(columnDefinition = "TEXT")
    private String thumbnail2;
    @Column(columnDefinition = "TEXT")
    private String thumbnail3;
    @Column(columnDefinition = "TEXT")
    private String thumbnail4;
}