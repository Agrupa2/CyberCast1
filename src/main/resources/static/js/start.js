const sounds = [
    { name: "Goofy Laugh", category: "goofy sounds", src: "sounds/goofy_laugh.mp3" },
    { name: "Donald Duck", category: "goofy sounds", src: "sounds/donald.mp3" },
    { name: "Opera Singer", category: "singers", src: "sounds/opera.mp3" },
    { name: "Pop Singer", category: "singers", src: "sounds/pop.mp3" },
    { name: "Movie Explosion", category: "movies", src: "sounds/explosion.mp3" },
    { name: "Lightsaber", category: "movies", src: "sounds/lightsaber.mp3" }
];

const searchInput = document.getElementById("search");
const categorySelect = document.getElementById("category");
const previewContainer = document.getElementById("preview-container");

// Función para filtrar y mostrar sonidos
function updateSounds() {
    const searchText = searchInput.value.toLowerCase();
    const selectedCategory = categorySelect.value;
    
    const filteredSounds = sounds.filter(sound => 
        (sound.name.toLowerCase().includes(searchText)) &&
        (selectedCategory === "all" || sound.category === selectedCategory)
    );

    displaySounds(filteredSounds);
}

// Función para mostrar sonidos
function displaySounds(soundList) {
    previewContainer.innerHTML = "";
    
    soundList.forEach(sound => {
        const card = document.createElement("div");
        card.classList.add("sound-card");
        card.innerHTML = `
            <h3>${sound.name}</h3>
            <button onclick="playSound('${sound.src}')">▶ Play</button>
        `;
        previewContainer.appendChild(card);
    });
}

// Función para reproducir sonido
function playSound(src) {
    const audio = new Audio(src);
    audio.play();
}

// Mostrar sonidos aleatorios en la previsualización inicial
function showRandomSounds() {
    const shuffled = sounds.sort(() => 0.5 - Math.random());
    const selected = shuffled.slice(0, 4);
    displaySounds(selected);
}

// Eventos
searchInput.addEventListener("input", updateSounds);
categorySelect.addEventListener("change", updateSounds);

// Cargar sonidos iniciales
showRandomSounds();
