const sounds = [
    {
        id: 1,
        title: "Nature Sound",
        description: "Relaxing forest ambiance",
        filePath: "/audio/nature.mp3",
        imagePath: "/images/nature.jpg",
        category: "Relax",
        duration: "3:20"
    },
    {
        id: 2,
        title: "Ocean Waves",
        description: "Soothing ocean waves",
        filePath: "/audio/ocean.mp3",
        imagePath: "/images/ocean.jpg",
        category: "Nature",
        duration: "4:45"
    },
    {
        id: 3,
        title: "Rain Sound",
        description: "Peaceful rain for sleep",
        filePath: "/audio/rain.mp3",
        imagePath: "/images/rain.jpg",
        category: "Meditation",
        duration: "5:10"
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
                    <div class="play" onclick="document.getElementById('audio-${sound.id}').play()">
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
            </div>
        `;
        
        container.appendChild(soundCard);
    });
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