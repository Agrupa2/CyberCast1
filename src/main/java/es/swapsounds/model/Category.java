package es.swapsounds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private List <Sound> soundList; // This is a list of sounds that belong to this category

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
        this.soundList = null;
    }

    public Category (String name) {
        this.name = name;
        this.soundList = null;
    }

    public Category() {
        //Used by JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sound> getSoundList() {
        return soundList;
    }

    public void setSoundList(List<Sound> soundList) {
        this.soundList = soundList;
    }

    public void addSound(Sound sound) {
        soundList.add(sound);
    }
}