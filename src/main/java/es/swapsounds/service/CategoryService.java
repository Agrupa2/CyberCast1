package es.swapsounds.service;

import es.swapsounds.model.Category;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private InMemoryStorage storage;

    public List<Category> getAllCategories() {
        return storage.getAllCategories();
    }

    public Category findOrCreateCategory(String categoryName) {
        return storage.findOrCreateCategory(categoryName);
    }
}
