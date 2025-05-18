package es.swapsounds.dto;

import java.util.List;
import java.util.stream.Collectors;

import es.swapsounds.model.Sound;

public record SoundDTO(
    long soundId,
    String title,
    String description,
    String duration,
    List<CategorySimpleDTO> categories
) {
    /** Constructor adicional que transforma de entidad Sound a DTO */
    public SoundDTO(Sound s) {
        this(
            s.getSoundId(),
            s.getTitle(),
            s.getDescription(),
            // Convierte duration a String; ajusta si quieres otro formato
            String.valueOf(s.getDuration()),
            s.getCategories().stream()
             .map(CategorySimpleDTO::new)
             .collect(Collectors.toList())
        );
    }
}
