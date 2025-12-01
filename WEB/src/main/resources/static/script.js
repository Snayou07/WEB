const API_URL = '/api/movies';

let isEditing = false;
let currentEditingId = null;

document.addEventListener('DOMContentLoaded', loadMovies);

// 1. ЗАВАНТАЖЕННЯ СПИСКУ
function loadMovies() {
    fetch(API_URL)
        .then(res => res.json())
        .then(movies => {
            const grid = document.getElementById('moviesGrid');
            const loading = document.getElementById('loading');
            grid.innerHTML = '';

            movies.forEach(movie => {
                const imageSrc = `https://placehold.co/200x300/2d2d2d/white?text=${encodeURIComponent(movie.title)}`;
                const voiceList = movie.availableVoiceovers ? movie.availableVoiceovers.join(', ') : '-';

                const card = document.createElement('div');
                card.className = 'movie-card';

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
                                    style="flex: 1; padding: 0; height: 36px; cursor: pointer; display: flex; justify-content: center; align-items: center; border: 1px solid #444; background: transparent; color: #ccc; border-radius: 4px; margin: 0;">
                                <i class="fa-solid fa-pen" style="margin-right: 5px;"></i> РЕД.
                            </button>

                            <button class="btn-action btn-delete" onclick="deleteMovie(${movie.id})"
                                    style="flex: 1; padding: 0; height: 36px; cursor: pointer; display: flex; justify-content: center; align-items: center; border: 1px solid #444; background: transparent; color: #ccc; border-radius: 4px; margin: 0;">
                                <i class="fa-solid fa-trash" style="margin-right: 5px;"></i> ВИД.
                            </button>

                        </div>
                    </div>
                `;
                grid.appendChild(card);
            });

            if (loading) loading.style.display = 'none';
        });
}

// 2. ОБРОБКА ФОРМИ (ДОДАВАННЯ ТА РЕДАГУВАННЯ)
const form = document.getElementById('addMovieForm') || document.getElementById('movieForm'); // Підтримка обох ID

if (form) {
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        clearErrors();

        // Збираємо дані
        const movieData = {
            title: document.getElementById('title').value,
            description: document.getElementById('description').value,
            releaseYear: document.getElementById('releaseYear').value,
            rating: document.getElementById('rating').value,
            availableVoiceovers: document.getElementById('voiceovers').value.split(',').map(s => s.trim())
        };

        let url = API_URL;
        let method = 'POST';

        // Якщо режим редагування - змінюємо URL і метод
        if (isEditing) {
            url = `${API_URL}/${currentEditingId}`;
            method = 'PUT';
        }

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(movieData)
        })
        .then(async response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 400) {
                // ВАЛІДАЦІЯ: Читаємо помилки від сервера
                const errors = await response.json();
                showErrors(errors);
                throw new Error('Validation Error');
            } else {
                throw new Error('Server Error');
            }
        })
        .then(() => {
            // Успіх
            resetForm();
            loadMovies();
        })
        .catch(error => console.log('Handled error:', error));
    });
}

// 3. ПОЧАТОК РЕДАГУВАННЯ
window.startEditing = function(id) {
    fetch(`${API_URL}/${id}`)
        .then(res => res.json())
        .then(movie => {
            // Заповнюємо поля
            document.getElementById('title').value = movie.title;
            document.getElementById('releaseYear').value = movie.releaseYear;
            document.getElementById('rating').value = movie.rating;
            document.getElementById('voiceovers').value = movie.availableVoiceovers ? movie.availableVoiceovers.join(', ') : '';
            document.getElementById('description').value = movie.description || '';

            // Вмикаємо режим
            isEditing = true;
            currentEditingId = id;

            // ЗМІНЮЄМО ІНТЕРФЕЙС
            const titleElement = document.getElementById('formTitle');
            if(titleElement) titleElement.innerHTML = '<i class="fa-solid fa-pen"></i> Редагувати фільм';

            const submitBtn = document.querySelector('.btn-submit') || document.querySelector('.btn-add');
            if(submitBtn) {
                submitBtn.innerText = 'ЗБЕРЕГТИ ЗМІНИ';
                submitBtn.style.backgroundColor = '#f39c12'; // Помаранчевий
            }

            // Показуємо кнопку скасування (якщо ще немає)
            let cancelBtn = document.getElementById('cancelEditBtn');
            if (!cancelBtn) {
                cancelBtn = document.createElement('button');
                cancelBtn.id = 'cancelEditBtn';
                cancelBtn.innerText = 'СКАСУВАТИ';
                cancelBtn.className = 'btn-cancel';
                cancelBtn.type = 'button';
                cancelBtn.onclick = resetForm;

                // Вставляємо перед кнопкою submit або в кінець форми
                form.appendChild(cancelBtn);
            } else {
                cancelBtn.style.display = 'block';
            }

            // Скрол до форми
            document.querySelector('.sidebar').scrollIntoView({ behavior: 'smooth' });
        });
};

// 4. СКИДАННЯ ФОРМИ (ПОВЕРНЕННЯ ДО ДОДАВАННЯ)
function resetForm() {
    isEditing = false;
    currentEditingId = null;
    if (form) form.reset();
    clearErrors();

    // Повертаємо інтерфейс
    const titleElement = document.getElementById('formTitle');
    if(titleElement) titleElement.innerHTML = '<i class="fa-solid fa-plus-circle"></i> Додати фільм';

    const submitBtn = document.querySelector('.btn-submit') || document.querySelector('.btn-add');
    if(submitBtn) {
        submitBtn.innerText = 'ДОДАТИ В БАЗУ';
        submitBtn.style.backgroundColor = '#27ae60'; // Зелений
    }

    const cancelBtn = document.getElementById('cancelEditBtn');
    if (cancelBtn) cancelBtn.style.display = 'none';
}

// 5. ВИДАЛЕННЯ
window.deleteMovie = function(id) {
    if (confirm('Видалити цей фільм?')) {
        fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            .then(res => {
                if (res.ok) {
                    // Якщо видалили той, що зараз редагуємо - скидаємо форму
                    if (currentEditingId === id) resetForm();
                    loadMovies();
                } else if (res.status === 403) {
                    alert('Видалення заборонено налаштуваннями сервера!');
                } else {
                    alert('Помилка видалення!');
                }
            });
    }
};

// 6. ВІДОБРАЖЕННЯ ПОМИЛОК
function showErrors(errors) {
    for (const [field, message] of Object.entries(errors)) {
        const input = document.getElementById(field);
        if (input) {
            input.classList.add('input-error'); // Червона рамка

            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-message'; // Червоний текст
            errorDiv.innerText = message;

            input.parentNode.appendChild(errorDiv);
        }
    }
}

function clearErrors() {
    document.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));
    document.querySelectorAll('.error-message').forEach(el => el.remove());
}