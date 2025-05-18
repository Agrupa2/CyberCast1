package es.swapsounds.DTO;

import java.util.List;

import org.mapstruct.Mapper;

import es.swapsounds.model.Sound;

@Mapper(componentModel = "spring")
public interface SoundMapper {

    SoundDTO toDTO(Sound sound);

    List<SoundDTO> toDTOs(List<Sound> sounds);
}
