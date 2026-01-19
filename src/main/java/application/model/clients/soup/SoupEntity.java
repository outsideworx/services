package application.model.clients.soup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "SOUP")
public final class SoupEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String category;
    @Transient
    private Boolean delete;
    private String description;
    @Column(columnDefinition = "TEXT")
    private String image;
    @Column(columnDefinition = "TEXT")
    private String thumbnail;
}