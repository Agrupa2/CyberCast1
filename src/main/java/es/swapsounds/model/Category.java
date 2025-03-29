package es.swapsounds.model;

import java.util.ArrayList;
import java.util.List;


public class Category {


    private long id;
    private String name;
    private List<Sound> sounds = new ArrayList<>(); // This is a list of sounds that belong to this category

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
        this.sounds = null;
    }

    public Category (String name) {
        this.name = name;
        this.sounds = null;
    }

    public Category() {
        //Used by JPA
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sound> getSoundList() {
        return sounds;
    }

    public void setSoundList(List<Sound> soundList) {
        this.sounds = soundList;
    }

    public void addSound(Sound sound) {
        sounds.add(sound);
    }
}