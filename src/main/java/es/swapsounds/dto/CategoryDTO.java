package es.swapsounds.DTO;

import java.util.List;

public record CategoryDTO(
                Long id,
                String name,
                List<SoundDTO> sounds) {
}
