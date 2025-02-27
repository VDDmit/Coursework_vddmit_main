document.addEventListener("DOMContentLoaded", async function () {
    try {
        await loadUsers();
        await loadProjects();
        document.getElementById("task-form").addEventListener("submit", createTask);
    } catch (error) {
        console.error("Ошибка при инициализации:", error);
    }
});

/** Загружает список пользователей */
async function loadUsers() {
    try {
        const response = await fetchWithAuth("/api/users/list");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

        const users = await response.json();
        console.log("Загруженные пользователи:", users); // Лог для проверки

        const userSelect = document.getElementById("task-user");
        userSelect.innerHTML = '<option value="">Выберите пользователя...</option>';

        users.forEach(user => {
            const option = document.createElement("option");
            option.value = user.id;
            option.textContent = user.username;
            userSelect.appendChild(option);
        });

        if (users.length === 0) {
            userSelect.innerHTML = '<option value="">Нет доступных пользователей</option>';
        }
    } catch (error) {
        console.error("Ошибка при загрузке пользователей:", error);
    }
}

/** Загружает список проектов */
async function loadProjects() {
    try {
        const response = await fetchWithAuth("/api/projects/with-members");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

        const projects = await response.json();
        console.log("Загруженные проекты:", projects); // Лог для проверки

        const projectSelect = document.getElementById("task-project");
        projectSelect.innerHTML = '<option value="">Выберите проект...</option>';

        projects.forEach(project => {
            const option = document.createElement("option");
            option.value = project.id;
            option.textContent = project.name;
            projectSelect.appendChild(option);
        });

        if (projects.length === 0) {
            projectSelect.innerHTML = '<option value="">Нет доступных проектов</option>';
        }
    } catch (error) {
        console.error("Ошибка при загрузке проектов:", error);
    }
}

/** Создает задачу */
async function createTask(event) {
    event.preventDefault();

    const title = document.getElementById("task-title").value.trim();
    const description = document.getElementById("task-desc").value.trim();
    const userId = document.getElementById("task-user").value;
    const projectId = document.getElementById("task-project").value;
    const status = document.getElementById("task-status").value;
    const xp = parseInt(document.getElementById("task-xp").value, 10);

    if (!title || !description || !userId || !projectId || !xp) {
        alert("Заполните все обязательные поля!");
        return;
    }

    try {
        console.log("Создание задачи с данными:", {title, description, userId, projectId, status, xp});

        const response = await fetchWithAuth("/api/tasks", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                title,
                description,
                status,
                xp,
                assignedUser: {id: userId},
                project: {id: projectId}
            })
        });

        if (!response.ok) throw new Error(`Ошибка создания задачи: ${response.status}`);

        alert("Задача успешно создана!");
        document.getElementById("task-form").reset();
    } catch (error) {
        console.error("Ошибка при создании задачи:", error);
    }
}
