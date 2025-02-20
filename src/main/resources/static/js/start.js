const sounds = [
    {
        id: 1,
        title: "El diablo que malditos tenis",
        description: "He likes the shoes of his homie",
        filePath: "/audio/el-diablo-que-malditos-tenis.mp3",
        imagePath: "/images/el-diablo-que-malditos-tenis.png",
        category: "Meme",
        duration: "0:04"
    },
    {
        id: 2,
        title: "Betis Anthem",
        description: "Betis Anthem for the fans",
        filePath: "/audio/betis.mp3",
        imagePath: "/images/betis.png",
        category: "Football",
        duration: "0:07"
    },
    {
        id: 3,
        title: "CR7(SIUU)",
        description: "Cristiano Ronaldo's celebration",
        filePath: "/audio/CR7.mp3",
        imagePath: "/images/CR7.jpg",
        category: "Football",
        duration: "0:06"
    },
    {
        id: 4,
        title: "El senor de la noche",
        description: "When you are in the pick of the party, play this sound",
        filePath: "/audio/ElSenorDeLaNoche.mp3",
        imagePath: "/images/ElSenorDeLaNoche.jpg",
        category: "Party",
        duration: "0:18"
    }
];

// Elementos del DOM
const searchInput = document.getElementById("search");
const categorySelect = document.getElementById("category");

// Función auxiliar para renderizar sonidos
function renderSounds(soundList) {
    const container = document.getElementById("soundsContainer");
    container.innerHTML = ""; // Limpiar contenedor
    container.classList.add("sound-grid");

    soundList.forEach(sound => {
        const soundCard = document.createElement("div");
        soundCard.classList.add("sound-card");
        
        soundCard.innerHTML = `
            <div class="card-head">
                <img src="${sound.imagePath}" alt="${sound.title}" class="card-img">
                <div class="card-overlay">
                    <div class="play" onclick="playSound(${sound.id})">
                        <ion-icon name="play-circle-outline"></ion-icon>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <h3 class="card-title"><a href="/sounds/${sound.id}">${sound.title}</a></h3>
                <div class="card-info">
                    <span class="category">${sound.category}</span>
                    <span class="duration">${sound.duration} min</span>
                </div>
                <audio id="audio-${sound.id}" src="${sound.filePath}" preload="none"></audio>
                <button onclick="playSound(${sound.id})">Play</button>
            </div>
        `;
        
        container.appendChild(soundCard);
    });
}

// Función para reproducir sonido
function playSound(soundId) {
    const audioElement = document.getElementById(`audio-${soundId}`);
    if (audioElement) {
        audioElement.play();
    }
}

// 1. Función para cargar sonidos iniciales (muestra 4 sonidos aleatorios)
function loadInitialSounds() {
    renderSounds(sounds); // Mostrar todos los sonidos
}

// 2. Función para buscar sonidos
function searchSounds() {
    const searchText = searchInput.value.toLowerCase();
    const selectedCategory = categorySelect.value;

    const filteredSounds = sounds.filter(sound => 
        (sound.title.toLowerCase().includes(searchText)) &&
        (selectedCategory === "all" || sound.category === selectedCategory)
    );

    renderSounds(filteredSounds);
}

// Eventos
searchInput.addEventListener("input", searchSounds);
categorySelect.addEventListener("change", searchSounds);

// Cargar sonidos iniciales al iniciar
loadInitialSounds();