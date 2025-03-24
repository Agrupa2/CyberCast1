package es.swapsounds.model;

import java.util.List;
import java.util.Set;

//import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table (name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String name;
    //private List <Sound> sounds; // This is a list of sounds that belong to this category

    @ManyToMany (mappedBy = "categories") //this isn´t the primary identity of the relationship, MappedBy will help us to find all the sounds of one category
    private Set<Sound> sounds;

    public Category() {
        // JPA
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
        this.sounds = null;
    }

    public Category (String name) {
        this.name = name;
        this.sounds = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Sound> getSounds() {
        return sounds;
    }

    public void setSounds(Set<Sound> soundList) {
        this.sounds = soundList;
    }

    public void addSound(Sound sound) {
        sounds.add(sound);
    }
}
