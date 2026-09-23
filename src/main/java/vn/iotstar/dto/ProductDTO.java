package vn.iotstar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductDTO {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống.")
    @Size(
        max = 200,
        message = "Tên sản phẩm tối đa 200 ký tự."
    )
    private String name;

    @Size(
        max = 1000,
        message = "Mô tả tối đa 1000 ký tự."
    )
    private String description;

    @NotNull(message = "Giá không được để trống.")
    @DecimalMin(
        value = "0.0",
        inclusive = true,
        message = "Giá phải lớn hơn hoặc bằng 0."
    )
    private BigDecimal price;

    private String image;
    private Long userId;
    private String ownerName;
    private LocalDateTime createdAt;

    public ProductDTO() {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(
            String ownerName
    ) {
        this.ownerName = ownerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }
}