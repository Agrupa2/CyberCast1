package es.swapsounds.model;

import java.util.List;

public class Category {

    private String id;
    private String name;
    private List <Sound> soundList; // This is a list of sounds that belong to this category

     public Category(String id, String name) {
        this.id = id;
        this.name = name;
        this.soundList = null;
    }

    public Category (String name) {
        this.name = name;
        this.soundList = null;
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
