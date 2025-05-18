package es.swapsounds.dto;

import es.swapsounds.model.Category;

public record CategorySimpleDTO(
    long   id,
    String name
) {
    /** Este es el constructor que necesitas */
    public CategorySimpleDTO(Category c) {
        this(c.getId(), c.getName());
    }
}
