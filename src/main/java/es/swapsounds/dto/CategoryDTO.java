package es.swapsounds.dto;

import java.util.List;

public record CategoryDTO(
                Long id,
                String name,
                List<SoundDTO> sounds) {
}
