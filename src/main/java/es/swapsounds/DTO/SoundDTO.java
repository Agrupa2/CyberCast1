package es.swapsounds.DTO;

import java.util.List;

public record SoundDTO(
        long soundId,
        String title,
        String description,
        String duration,
        List<CategorySimpleDTO> categories
) 
{}

