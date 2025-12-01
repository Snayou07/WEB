const API_URL = '/api/users';
let isEditing = false;
let currentEditingId = null;

document.addEventListener('DOMContentLoaded', loadUsers);

// 1. ЗАВАНТАЖЕННЯ КОРИСТУВАЧІВ (GET)
function loadUsers() {
    fetch(API_URL)
        .then(response => response.json())
        .then(users => {
            const grid = document.getElementById('usersGrid');
            const loading = document.getElementById('loading');

            grid.innerHTML = '';

            users.forEach(user => {
                // Генеруємо аватарку
                const avatarSrc = `https://ui-avatars.com/api/?name=${user.username}&background=random&color=fff`;

                const card = document.createElement('div');
                card.className = 'user-card';

                // Вставляємо HTML з жорсткими стилями для кнопок
                card.innerHTML = `
                    <div class="user-avatar">
                        <img src="${avatarSrc}" alt="${user.username}">
                    </div>
                    <div class="user-info">
                        <h4>${user.username}</h4>
                        <p>${user.email}</p>
                        <p style="margin-top:5px; font-size:11px; color:#666;">ID: ${user.id}</p>

                        <div class="card-actions" style="display: flex; gap: 10px; width: 100%; margin-top: 15px; align-items: stretch;">

                            <button class="btn-action btn-edit" onclick="startEditing(${user.id})"
                                    style="flex: 1; padding: 0; height: 36px; cursor: pointer; display: flex; justify-content: center; align-items: center; border: 1px solid #444; background: transparent; color: #ccc; border-radius: 4px; margin: 0;">
                                <i class="fa-solid fa-pen" style="margin-right: 5px;"></i> РЕД.
                            </button>

                            <button class="btn-action btn-delete" onclick="deleteUser(${user.id})"
                                    style="flex: 1; padding: 0; height: 36px; cursor: pointer; display: flex; justify-content: center; align-items: center; border: 1px solid #444; background: transparent; color: #ccc; border-radius: 4px; margin: 0;">
                                <i class="fa-solid fa-trash" style="margin-right: 5px;"></i> ВИД.
                            </button>

                        </div>
                    </div>
                `;
                grid.appendChild(card);
            });

            if (loading) loading.style.display = 'none';
        })
        .catch(error => console.error('Помилка:', error));
}

document.getElementById('userForm').addEventListener('submit', function(event) {
    event.preventDefault();
    clearErrors();

    const userData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value
    };

    let url = API_URL;
    let method = 'POST';

    if (isEditing) {
        url = `${API_URL}/${currentEditingId}`;
        method = 'PUT';
    }

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
    })
    .then(async response => {
        if (response.ok) return response.json();

        // ОБРОБКА ПОМИЛОК (400 Bad Request)
        if (response.status === 400) {
            const errors = await response.json();
            showErrors(errors); // Показуємо червоні поля
            throw new Error('Validation error');
        }
        throw new Error('Server error');
    })
    .then(() => {
        resetForm();
        loadUsers();
    })
    .catch(err => console.log(err));
});

function startEditing(id) {
    fetch(`${API_URL}/${id}`).then(res => res.json()).then(user => {
        document.getElementById('username').value = user.username;
        document.getElementById('email').value = user.email;

        isEditing = true;
        currentEditingId = id;

        // ЗМІНА ІНТЕРФЕЙСУ
        const title = document.getElementById('formTitle');
        title.innerHTML = '<i class="fa-solid fa-user-pen"></i> Редагувати користувача'; // Зміна заголовку

        const submitBtn = document.getElementById('submitBtn');
        submitBtn.innerText = 'ЗБЕРЕГТИ ЗМІНИ'; // Зміна тексту кнопки
        submitBtn.style.backgroundColor = '#f39c12';

        document.getElementById('cancelBtn').style.display = 'block';
    });
}

function resetForm() {
    isEditing = false;
    currentEditingId = null;
    document.getElementById('userForm').reset();
    clearErrors();

    // ПОВЕРНЕННЯ ІНТЕРФЕЙСУ
    document.getElementById('formTitle').innerHTML = '<i class="fa-solid fa-user-plus"></i> Створити користувача';

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.innerText = 'СТВОРИТИ';
    submitBtn.style.backgroundColor = '#27ae60';

    document.getElementById('cancelBtn').style.display = 'none';
}

function deleteUser(id) {
    if (confirm('Видалити?')) {
        fetch(`${API_URL}/${id}`, { method: 'DELETE' }).then(() => loadUsers());
    }
}

function showErrors(errors) {
    for (const [field, message] of Object.entries(errors)) {
        const input = document.getElementById(field);
        if (input) {
            input.classList.add('input-error');
            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            errorDiv.innerText = message;
            input.parentNode.appendChild(errorDiv);
        }
    }
}

function clearErrors() {
    document.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));
    document.querySelectorAll('.error-message').forEach(el => el.remove());
}