package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {


    @Mapping(
            target = "userId",
            source = "user.id"
    )

    @Mapping(
            target = "ownerName",
            source = "user.fullName"
    )

    ProductDTO toDTO(
            Product product
    );


    @Mapping(
            target = "user",
            ignore = true
    )

    Product toEntity(
            ProductDTO dto
    );
}