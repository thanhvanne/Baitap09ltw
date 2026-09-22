package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {


    @Mapping(
            target = "roleId",
            source = "role.id"
    )


    @Mapping(
            target = "roleName",
            source = "role.name"
    )


    /*
     * Không gọi user.getProducts().size()
     *
     * UserService sẽ query COUNT trực tiếp.
     */
    @Mapping(
            target = "productCount",
            ignore = true
    )

    UserDTO toDTO(
            User user
    );


    @Mapping(
            target = "role",
            ignore = true
    )


    @Mapping(
            target = "products",
            ignore = true
    )


    @Mapping(
            target = "password",
            ignore = true
    )

    User toEntity(
            UserDTO dto
    );
}