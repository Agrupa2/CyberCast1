package es.swapsounds.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;


@Entity
public class Category {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @ManyToMany(mappedBy = "categories")
    private List<Sound> sounds = new ArrayList<>(); // This is a list of sounds that belong to this category

    public Category(String name) {
        this.name = name;
        this.sounds = new ArrayList<>(); // Eliminar null
    }

    public Category() {
        this.sounds = new ArrayList<>(); // Inicializar en constructor vacío
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

    public List<Sound> getSounds() {
        return sounds;
    }
}