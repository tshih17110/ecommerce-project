package com.github.ecommerce_project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.github.ecommerce_project.dtos.UserRegistrationDto;
import com.github.ecommerce_project.dtos.UserResponseDto;
import com.github.ecommerce_project.models.User;

@Mapper(componentModel = "spring", uses = PasswordEncodingMapper.class)
public interface UserMapper {

    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedBy = EncodedMapping.class)
    User toEntity(UserRegistrationDto dto);

}
