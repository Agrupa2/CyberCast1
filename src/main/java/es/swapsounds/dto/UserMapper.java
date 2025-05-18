package es.swapsounds.dto;

import java.util.List;

import org.mapstruct.Mapper;

import es.swapsounds.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    List<UserDTO> toDtoList(List<User> users);

}
