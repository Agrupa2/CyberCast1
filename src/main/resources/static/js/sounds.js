document.addEventListener('DOMContentLoaded', () => {
    const overlay     = document.getElementById('overlaySpinner');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const container   = document.getElementById('soundsContainer');
    const MIN_VISIBLE = 500; // ms

    if (!loadMoreBtn || !container) return;

    // Función para inicializar controles de audio en las nuevas tarjetas
    function initAudioControls(card) {
        const audio = card.querySelector('audio');
        const playPauseBtn = card.querySelector('.play-pause-btn');
        const progressBar = card.querySelector('.progress-bar');
        const progress = card.querySelector('.progress');

        if (!audio || !playPauseBtn || !progressBar || !progress) return;

        playPauseBtn.addEventListener('click', () => {
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
            progress.style.width = percent + '%';
        });

        progressBar.addEventListener('click', (e) => {
            const rect = progressBar.getBoundingClientRect();
            const clickX = e.clientX - rect.left;
            const percent = (clickX / rect.width);
            audio.currentTime = percent * audio.duration;
        });
    }

    // Inicializa controles en las tarjetas ya existentes
    document.querySelectorAll('.sound-card').forEach(initAudioControls);

    loadMoreBtn.addEventListener('click', () => {
        const start = Date.now();
        if (overlay) overlay.classList.remove('hidden');
        loadMoreBtn.disabled = true;

        const currentPage = parseInt(loadMoreBtn.dataset.page, 10) || 0;
        const nextPage    = currentPage + 1;
        const size        = loadMoreBtn.dataset.size || 10;
        const query       = loadMoreBtn.dataset.query || '';
        const category    = loadMoreBtn.dataset.category || 'all';

        fetch(`/api/sounds?query=${encodeURIComponent(query)}&category=${encodeURIComponent(category)}&page=${nextPage}&size=${size}`)
            .then(res => {
                if (!res.ok) throw new Error(`Fetch error ${res.status}`);
                return res.json();
            })
            .then(data => {
                // Cambia data.sounds por data.content
                (data.content || []).forEach(s => {
                    const card = document.createElement('div');
                    card.className = 'sound-card';
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
                            ${(s.categories || []).map(cat => `<span class="category">${cat.name}</span>`).join('')}
                        </div>
                        <span class="duration">${s.duration} min</span>
                    </div>
                    <div class="audio-container">
                        <audio class="custom-audio">
                            <source src="/sounds/audio/${s.soundId}" type="audio/mpeg">
                            Your browser does not support the audio element.
                        </audio>
                        <div class="audio-controls">
                            <button class="play-pause-btn">▶️</button>
                            <div class="progress-bar">
                                <div class="progress"></div>
                            </div>
                        </div>
                    </div>
                </div>`;
                    container.appendChild(card);
                    initAudioControls(card);

                });

                // Actualiza el data-page al nuevo valor
                loadMoreBtn.dataset.page = nextPage; // 'number' es la página actual en Spring

                // Oculta el botón si no hay más páginas
                const hasNext = !data.last; // 'last' es true si es la última página
                loadMoreBtn.style.display = hasNext ? 'inline-block' : 'none';
            })
            .catch(err => {
                console.error('Error al cargar más sonidos:', err);
            })
            .finally(() => {
                const elapsed = Date.now() - start;
                const wait = Math.max(0, MIN_VISIBLE - elapsed);
                setTimeout(() => {
                    if (overlay) overlay.classList.add('hidden');
                    loadMoreBtn.disabled = false;
                }, wait);
        });
    });

    // Previene menú contextual en audio
    document.addEventListener('contextmenu', function (e) {
        if (e.target.tagName === 'AUDIO') {
            e.preventDefault();
        }
    });
});