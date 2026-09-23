package vn.iotstar.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(
            name = "idx_products_name",
            columnList = "name"
        ),
        @Index(
            name = "idx_products_user_id",
            columnList = "user_id"
        )
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        length = 200,
        columnDefinition = "nvarchar(200)"
    )
    private String name;

    @Column(
        length = 1000,
        columnDefinition = "nvarchar(1000)"
    )
    private String description;

    @Column(
        nullable = false,
        precision = 18,
        scale = 2
    )
    private BigDecimal price;

    @Column(length = 500)
    private String image;

    @Column(
        name = "image_public_id",
        length = 500
    )
    private String imagePublicId;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt =
        LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImagePublicId() {
        return imagePublicId;
    }

    public void setImagePublicId(
            String imagePublicId
    ) {
        this.imagePublicId = imagePublicId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}