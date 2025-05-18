document.addEventListener("DOMContentLoaded", () => {
    const overlay = document.getElementById("overlaySpinner");
    const loadMoreBtn = document.getElementById("loadMoreBtn");
    const container = document.getElementById("soundsContainer");
    const MIN_VISIBLE = 500;

    // Función para inicializar controles de audio
    function initAudioControls(card) {
        const audio = card.querySelector('audio');
        const playPauseBtn = card.querySelector('.play-pause-btn');
        const progressBar = card.querySelector('.progress-bar');
        const progress = card.querySelector('.progress');

        const pauseOtherAudios = () => {
            document.querySelectorAll('audio').forEach(otherAudio => {
                if (otherAudio !== audio && !otherAudio.paused) {
                    otherAudio.pause();
                    otherAudio.parentElement
                             .querySelector('.play-pause-btn').textContent = '▶️';
                }
            });
        };

        playPauseBtn.addEventListener('click', () => {
            pauseOtherAudios();
            if (audio.paused) {
                audio.play();
                playPauseBtn.textContent = '⏸️';
            } else {
                audio.pause();
                playPauseBtn.textContent = '▶️';
            }
        });

        audio.addEventListener('timeupdate', () => {
            const percent = (audio.currentTime / audio.duration) * 100;
            progress.style.width = `${percent}%`;
        });

        progressBar.addEventListener('click', (e) => {
            const rect = progressBar.getBoundingClientRect();
            audio.currentTime = (e.clientX - rect.left) / rect.width * audio.duration;
        });

        card.dataset.initialized = "true"; // Marcar como inicializado
    }

    // Inicializar controles existentes al cargar
    document.querySelectorAll('.sound-card:not([data-initialized])').forEach(initAudioControls);

    loadMoreBtn.addEventListener("click", () => {
        const start = Date.now();
        overlay.classList.remove("hidden");
        loadMoreBtn.disabled = true;

        const currentPage = parseInt(loadMoreBtn.dataset.page, 10);
        const nextPage = currentPage + 1;
        const size = loadMoreBtn.dataset.size || 10;
        const query = loadMoreBtn.dataset.query || "";
        const category = loadMoreBtn.dataset.category || "all";

        fetch(`/api/sounds?query=${encodeURIComponent(query)}&category=${category}&page=${nextPage}&size=${size}`)
            .then((res) => {
                if (!res.ok) throw new Error(`Fetch error ${res.status}`);
                return res.json();
            })
            .then((data) => {
                // Crear fragmento para mejor rendimiento
                const fragment = document.createDocumentFragment();
                
                data.sounds.forEach((s) => {
                    const card = document.createElement("div");
                    card.className = "sound-card";
                    card.innerHTML = `
                        <div class="card-head">
                            <img src="/sounds/image/${s.soundId}" alt="${s.title}" class="card-img">
                            <div class="card-overlay">
                                <div class="play"><ion-icon name="play-circle-outline"></ion-icon></div>
                            </div>
                        </div>
                        <div class="card-body">
                            <h3 class="card-title">
                                <a href="/sounds/${s.soundId}">${s.title}</a>
                            </h3>
                            <div class="card-info">
                                <div class="categories">
                                    ${s.categories.map((cat) => `<span class="category">${cat.name}</span>`).join("")}
                                </div>
                                <span class="duration">${s.duration} min</span>
                            </div>
                            <div class="audio-container">
                                <audio class="custom-audio">
                                    <source src="/sounds/audio/${s.soundId}" type="audio/mpeg">
                                </audio>
                                <div class="audio-controls">
                                    <button class="play-pause-btn">▶️</button>
                                    <div class="progress-bar">
                                        <div class="progress"></div>
                                    </div>
                                </div>
                            </div>
                        </div>`;
                    fragment.appendChild(card);
                });

                container.appendChild(fragment);

                // Inicializar controles en nuevos elementos
                container.querySelectorAll('.sound-card:not([data-initialized])').forEach(initAudioControls);

                // Actualizar estado
                loadMoreBtn.dataset.page = data.currentPage;
                loadMoreBtn.style.display = (data.currentPage + 1 < data.totalPages) ? "inline-block" : "none";
            })
            .catch((err) => {
                console.error("Error al cargar más sonidos:", err);
            })
            .finally(() => {
                const elapsed = Date.now() - start;
                const wait = Math.max(0, MIN_VISIBLE - elapsed);
                setTimeout(() => {
                    overlay.classList.add("hidden");
                    loadMoreBtn.disabled = false;
                }, wait);
            });
    });

    document.addEventListener('contextmenu', (e) => {
        if (e.target.tagName === 'AUDIO') e.preventDefault();
    });
});