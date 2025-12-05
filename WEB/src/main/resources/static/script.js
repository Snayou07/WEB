const API_URL = '/api/movies';

let isEditing = false;
let currentEditingId = null;

document.addEventListener('DOMContentLoaded', () => {
    loadMovies();
    setupForm();
});

// ========== 1. ЗАВАНТАЖЕННЯ СПИСКУ ==========
function loadMovies() {
    const loading = document.getElementById('loading');
    const grid = document.getElementById('moviesGrid');

    if (loading) loading.style.display = 'block';

    fetch(API_URL)
        .then(res => {
            if (!res.ok) throw new Error('Помилка завантаження');
            return res.json();
        })
        .then(movies => {
            grid.innerHTML = '';

            if (movies.length === 0) {
                grid.innerHTML = '<p style="color: #888; text-align: center;">Фільмів ще немає</p>';
                return;
            }

            movies.forEach(movie => {
                const imageSrc = `https://placehold.co/200x300/2d2d2d/white?text=${encodeURIComponent(movie.title)}`;
                const voiceList = movie.availableVoiceovers?.join(', ') || '-';

                const card = document.createElement('div');
                card.className = 'movie-card';

                // ТУТ ВНЕСЕНО ЗМІНИ: Додано атрибути style="" для кнопок і контейнера
                card.innerHTML = `
                    <div class="poster-wrapper">
                        <span class="rating-badge"><i class="fa-solid fa-star"></i> ${movie.rating}</span>
                        <img src="${imageSrc}" alt="${movie.title}">
                    </div>
                    <div class="movie-info">
                        <div class="movie-title" title="${movie.title}">${movie.title}</div>
                        <div class="movie-meta">
                            <span>${movie.releaseYear}</span>
                            <span>${voiceList}</span>
                        </div>

                        <div class="card-actions" style="display: flex; gap: 10px; width: 100%; margin-top: 15px; align-items: stretch;">

                            <button class="btn-action btn-edit" onclick="startEditing(${movie.id})"
                                style="flex: 1; margin: 0; padding: 0; height: 38px; display: flex; justify-content: center; align-items: center; border: 1px solid #444; background: transparent; color: #ccc; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold;">
                                <i class="fa-solid fa-pen" style="margin-right: 5px;"></i> РЕД.
                            </button>

                            <button class="btn-action btn-delete" onclick="deleteMovie(${movie.id})"
                                style="flex: 1; margin: 0; padding: 0; height: 38px; display: flex; justify-content: center; align-items: center; border: 1px solid #444; background: transparent; color: #ccc; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold;">
                                <i class="fa-solid fa-trash" style="margin-right: 5px;"></i> ВИД.
                            </button>

                        </div>
                    </div>
                `;
                grid.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Помилка:', error);
            grid.innerHTML = '<p style="color: #e74c3c;">Помилка завантаження фільмів</p>';
        })
        .finally(() => {
            if (loading) loading.style.display = 'none';
        });
}

// ========== 2. НАЛАШТУВАННЯ ФОРМИ ==========
function setupForm() {
    const form = document.getElementById('movieForm'); // Переконайся, що в HTML ID форми 'movieForm' (або 'addMovieForm')
    const addForm = document.getElementById('addMovieForm'); // Про всяк випадок перевіряємо обидва
    const targetForm = form || addForm;

    const cancelBtn = document.getElementById('cancelBtn') || document.getElementById('cancelEditBtn');

    if (!targetForm) {
        // console.error('Форма не знайдена!'); // Можна розкоментувати для налагодження
        return;
    }

    // Видаляємо старі слухачі (через клонування), щоб не дублювались при перезавантаженні сторінки (якщо SPA)
    const newForm = targetForm.cloneNode(true);
    targetForm.parentNode.replaceChild(newForm, targetForm);

    newForm.addEventListener('submit', handleSubmit);

    if (cancelBtn) {
        // Те саме для кнопки скасування
        const newCancel = cancelBtn.cloneNode(true);
        cancelBtn.parentNode.replaceChild(newCancel, cancelBtn);
        newCancel.addEventListener('click', resetForm);
        // Якщо кнопка створена динамічно, її треба приховати
        newCancel.style.display = 'none';
    }
}

// ========== 3. ОБРОБКА ВІДПРАВКИ ФОРМИ ==========
function handleSubmit(event) {
    event.preventDefault();
    clearErrors();

    const movieData = {
        title: document.getElementById('title').value.trim(),
        description: document.getElementById('description').value.trim(),
        releaseYear: parseInt(document.getElementById('releaseYear').value),
        rating: parseFloat(document.getElementById('rating').value),
        availableVoiceovers: document.getElementById('voiceovers').value
            .split(',')
            .map(s => s.trim())
            .filter(s => s.length > 0)
    };

    const url = isEditing ? `${API_URL}/${currentEditingId}` : API_URL;
    const method = isEditing ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(movieData)
    })
    .then(async response => {
        if (response.ok) {
            return response.json();
        } else if (response.status === 400) {
            const errors = await response.json();
            showErrors(errors);
            throw new Error('Validation Error');
        } else {
            throw new Error('Server Error');
        }
    })
    .then(() => {
        resetForm();
        loadMovies();
        // showNotification(isEditing ? 'Фільм оновлено!' : 'Фільм додано!', 'success');
        alert(isEditing ? 'Фільм оновлено!' : 'Фільм додано!');
    })
    .catch(error => {
        if (error.message !== 'Validation Error') {
            // showNotification('Помилка сервера', 'error');
            console.error(error);
        }
    });
}

// ========== 4. ПОЧАТОК РЕДАГУВАННЯ ==========
window.startEditing = function(id) {
    fetch(`${API_URL}/${id}`)
        .then(res => {
            if (!res.ok) throw new Error('Фільм не знайдено');
            return res.json();
        })
        .then(movie => {
            // Заповнюємо форму
            document.getElementById('title').value = movie.title;
            document.getElementById('releaseYear').value = movie.releaseYear;
            document.getElementById('rating').value = movie.rating;
            document.getElementById('voiceovers').value = movie.availableVoiceovers?.join(', ') || '';
            document.getElementById('description').value = movie.description || '';

            // Режим редагування
            isEditing = true;
            currentEditingId = id;

            // Змінюємо UI
            const titleEl = document.getElementById('formTitle');
            if(titleEl) titleEl.innerHTML = '<i class="fa-solid fa-pen"></i> Редагувати фільм';

            const submitBtn = document.getElementById('submitBtn') || document.querySelector('.btn-submit') || document.querySelector('.btn-add');
            if(submitBtn) {
                submitBtn.textContent = 'ЗБЕРЕГТИ ЗМІНИ';
                submitBtn.style.backgroundColor = '#f39c12';
            }

            // Шукаємо кнопку скасування (або створюємо, або показуємо існуючу)
            let cancelBtn = document.getElementById('cancelBtn') || document.getElementById('cancelEditBtn');

            // Якщо кнопки в HTML немає, створюємо її динамічно (як було в минулих версіях)
            if (!cancelBtn) {
                 const form = document.getElementById('movieForm') || document.getElementById('addMovieForm');
                 cancelBtn = document.createElement('button');
                 cancelBtn.id = 'cancelEditBtn';
                 cancelBtn.innerText = 'СКАСУВАТИ';
                 cancelBtn.type = 'button';
                 // Прості стилі для динамічної кнопки
                 cancelBtn.style.width = '100%';
                 cancelBtn.style.backgroundColor = '#555';
                 cancelBtn.style.color = 'white';
                 cancelBtn.style.border = 'none';
                 cancelBtn.style.padding = '8px';
                 cancelBtn.style.marginTop = '10px';
                 cancelBtn.style.cursor = 'pointer';
                 cancelBtn.style.borderRadius = '3px';

                 cancelBtn.onclick = resetForm;
                 form.appendChild(cancelBtn);
            } else {
                cancelBtn.style.display = 'block';
            }

            // Скрол до форми
            document.querySelector('.sidebar').scrollIntoView({ behavior: 'smooth', block: 'start' });
        })
        .catch(error => {
            console.error(error);
        });
};

// ========== 5. СКИДАННЯ ФОРМИ ==========
function resetForm() {
    const form = document.getElementById('movieForm') || document.getElementById('addMovieForm');
    if (form) form.reset();

    isEditing = false;
    currentEditingId = null;
    clearErrors();

    // Повертаємо вигляд "додавання"
    const titleEl = document.getElementById('formTitle');
    if(titleEl) titleEl.innerHTML = '<i class="fa-solid fa-plus-circle"></i> Додати фільм';

    const submitBtn = document.getElementById('submitBtn') || document.querySelector('.btn-submit') || document.querySelector('.btn-add');
    if(submitBtn) {
        submitBtn.textContent = 'ДОДАТИ В БАЗУ';
        submitBtn.style.backgroundColor = '#27ae60';
    }

    const cancelBtn = document.getElementById('cancelBtn') || document.getElementById('cancelEditBtn');
    if (cancelBtn) cancelBtn.style.display = 'none';
}

// ========== 6. ВИДАЛЕННЯ ==========
window.deleteMovie = function(id) {
    if (!confirm('Ви впевнені, що хочете видалити цей фільм?')) return;

    fetch(`${API_URL}/${id}`, { method: 'DELETE' })
        .then(res => {
            if (res.ok || res.status === 204) {
                // showNotification('Фільм видалено!', 'success');
                if (currentEditingId === id) resetForm();
                loadMovies();
            } else if (res.status === 403) {
                alert('Видалення заборонено налаштуваннями сервера');
            } else if (res.status === 404) {
                alert('Фільм не знайдено');
            } else {
                throw new Error('Помилка видалення');
            }
        })
        .catch(error => {
            console.error(error);
        });
};

// ========== 7. ВІДОБРАЖЕННЯ ПОМИЛОК ВАЛІДАЦІЇ ==========
function showErrors(errors) {
    for (const [field, message] of Object.entries(errors)) {
        const input = document.getElementById(field);
        if (input) {
            input.classList.add('input-error');

            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            errorDiv.textContent = message;
            // Додаємо стиль помилки прямо тут, якщо CSS не працює
            errorDiv.style.color = '#e74c3c';
            errorDiv.style.fontSize = '11px';
            errorDiv.style.fontWeight = 'bold';

            input.parentNode.appendChild(errorDiv);
        }
    }
}

function clearErrors() {
    document.querySelectorAll('.input-error').forEach(el =>
        el.classList.remove('input-error')
    );
    document.querySelectorAll('.error-message').forEach(el =>
        el.remove()
    );
}