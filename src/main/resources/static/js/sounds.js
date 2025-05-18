document.addEventListener('DOMContentLoaded', () => {
    const overlay   = document.getElementById('overlaySpinner');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const container = document.getElementById('soundsContainer');
    const MIN_VISIBLE = 500; // ms

    loadMoreBtn.addEventListener('click', () => {
        const start = Date.now();
        // Show overlay spinner
        overlay.classList.remove('hidden');
        loadMoreBtn.disabled = true;

        const currentPage = parseInt(loadMoreBtn.dataset.page, 10);
        const nextPage    = currentPage + 1;
        const size        = loadMoreBtn.dataset.size || 8;
        const query       = loadMoreBtn.dataset.query || '';
        const category    = loadMoreBtn.dataset.category || 'all';

        fetch(`/api/sounds?query=${encodeURIComponent(query)}&category=${category}&page=${nextPage}&size=${size}`)
            .then(res => {
                if (!res.ok) throw new Error(`Fetch error ${res.status}`);
                return res.json();
        })
            .then(data => {
                // Insert new sounds
                data.sounds.forEach(s => {
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
                    ${s.categories.map(cat => <span class="category">${cat.name}</span>).join('')}
                  </div>
                  <span class="duration">${s.duration} min</span>
                </div>
                <div class="audio-container">
                  <audio class="custom-audio">
                    <source src="/sounds/audio/${s.soundId}" type="audio/mpeg">
                    Your browser does not support the audio element.
                  </audio>
                  <div class="audio-controls">
                    <button class="play-pause-btn">▶</button>
                    <div class="progress-bar">
                      <div class="progress"></div>
                    </div>
                  </div>
                </div>
              </div>`;
                    container.appendChild(card);
                });

                // Upload new audio elements
                loadMoreBtn.dataset.page = data.currentPage;
                const hasMore = data.currentPage + 1 < data.totalPages;
                loadMoreBtn.style.display = hasMore ? 'inline-block' : 'none';
            })
            .catch(err => {
                console.error('Error al cargar más sonidos:', err);
            })
            .finally(() => {
                const elapsed = Date.now() - start;
                const wait = Math.max(0, MIN_VISIBLE - elapsed);
                setTimeout(() => {
                    overlay.classList.add('hidden');
                    loadMoreBtn.disabled = false;
                }, wait);
            });
    });
});