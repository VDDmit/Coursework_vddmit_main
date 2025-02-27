document.addEventListener("DOMContentLoaded", async function () {
    await loadTasks();
});

/** Глобальный массив задач */
let allTasks = [];

/** Загрузка задач из API */
async function loadTasks() {
    const container = document.getElementById("tasks-container");
    try {
        const response = await fetchWithAuth("/api/tasks/list-from-users-project");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

        allTasks = await response.json();
        renderFilters();
        filterAndSortTasks();
    } catch (error) {
        console.error("Ошибка при загрузке задач:", error);
        container.innerHTML = "<p class='text-danger text-center'>Не удалось загрузить задачи</p>";
    }
}


/** Рендер полей поиска и сортировки */
function renderFilters() {
    const container = document.getElementById("tasks-container");
    container.innerHTML = `
        <div class="mb-3 d-flex justify-content-between">
            <input type="text" id="taskSearch" class="form-control me-2 w-50" placeholder="Поиск по названию">
            <select id="sortTasks" class="form-select w-auto me-2">
                <option value="">Без сортировки</option>
                <option value="xp_asc">XP (возрастание)</option>
                <option value="xp_desc">XP (убывание)</option>
                <option value="assigned_asc">Исполнитель (A-Z)</option>
                <option value="assigned_desc">Исполнитель (Z-A)</option>
            </select>
            <select id="filterStatus" class="form-select w-auto">
                <option value="">Все статусы</option>
                <option value="TODO">TODO</option>
                <option value="IN_PROGRESS">В работе</option>
                <option value="IN_REVIEW">На проверке</option>
                <option value="DONE">Выполнена</option>
            </select>
        </div>
        <div id="task-list"></div>
    `;

    document.getElementById("taskSearch").addEventListener("input", filterAndSortTasks);
    document.getElementById("sortTasks").addEventListener("change", filterAndSortTasks);
    document.getElementById("filterStatus").addEventListener("change", filterAndSortTasks);
}

/** Фильтрация и сортировка задач */
function filterAndSortTasks() {
    const searchQuery = document.getElementById("taskSearch").value.toLowerCase();
    const sortOption = document.getElementById("sortTasks").value;
    const statusFilter = document.getElementById("filterStatus").value;

    let filteredTasks = allTasks.filter(task =>
        task.title.toLowerCase().includes(searchQuery) &&
        (statusFilter === "" || task.status === statusFilter)
    );

    switch (sortOption) {
        case "xp_asc":
            filteredTasks.sort((a, b) => (a.xp || 0) - (b.xp || 0));
            break;
        case "xp_desc":
            filteredTasks.sort((a, b) => (b.xp || 0) - (a.xp || 0));
            break;
        case "assigned_asc":
            filteredTasks.sort((a, b) => (a.assignedUser?.username || "").localeCompare(b.assignedUser?.username || ""));
            break;
        case "assigned_desc":
            filteredTasks.sort((a, b) => (b.assignedUser?.username || "").localeCompare(a.assignedUser?.username || ""));
            break;
    }

    renderTasks(filteredTasks);
}

/** Отображение списка задач */
function renderTasks(tasks) {
    applyAccessRestrictions();

    const listContainer = document.getElementById("task-list");
    listContainer.innerHTML = tasks.length
        ? tasks.map(taskToHTML).join("")
        : "<p class='text-center text-muted'>Нет доступных задач</p>";
}


async function applyAccessRestrictions() {
    const response = await fetchWithAuth("/api/users/me");
    if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
    const user = await response.json();
    const userAccessLevel = user.role.accessLevel.trim().toUpperCase(); // Приводим к верхнему регистру


    document.querySelectorAll("[data-access-level]").forEach(element => {
        const requiredLevels = element.getAttribute("data-access-level")
            .split(",")
            .map(level => level.trim().toUpperCase());

        console.log(`Элемент: ${element.outerHTML}, Требуемые уровни: ${requiredLevels}`);

        if (!requiredLevels.includes(userAccessLevel)) {
            if (element.tagName === "OPTION") {
                element.remove();
            } else {
                element.style.display = "none";
            }
        }
    });
}


/** Генерация HTML-кода задачи */
function taskToHTML(task) {
    const statusBadge = getStatusBadge(task.status);
    const description = task.description || "Без описания";
    const xp = task.xp !== null ? `🎖️ XP: ${task.xp}` : "🎖️ XP: 0";
    const projectTitle = task.project?.name ? `📌 Проект: ${task.project.name}` : "📌 Проект: Не указан";
    const assignedUser = task.assignedUser?.username ? `👤 ${task.assignedUser.username}` : "👤 Не назначен";

    return `
        <div class="card bg-secondary text-light mb-3 shadow-sm">
            <div class="card-body p-3">
                <h6 class="card-title mb-2 fs-4">${task.title}</h6>
                <p class="card-text small text-muted mb-3">${description}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center gap-3">
                        <span class="text-light fs-5">${xp}</span>
                        <span class="text-light fs-6">${projectTitle}</span>
                        <span class="text-light fs-6">${assignedUser}</span>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <div class="dropdown">
                            <button class="btn btn-sm btn-outline-light dropdown-toggle d-flex align-items-center gap-1" type="button" id="dropdownMenuButton${task.id}" data-bs-toggle="dropdown" aria-expanded="false">
                                ${statusBadge}
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton${task.id}">
                                <li><a class="dropdown-item" href="#" onclick="changeTaskStatus('${task.id}', 'TODO')">📌 TODO</a></li>
                                <li><a class="dropdown-item" href="#" onclick="changeTaskStatus('${task.id}', 'IN_PROGRESS')">⏳ В работе</a></li>
                                <li><a class="dropdown-item" href="#" onclick="changeTaskStatus('${task.id}', 'IN_REVIEW')">🧐 На проверке</a></li>
                                <li><a class="dropdown-item" href="#" onclick="changeTaskStatus('${task.id}', 'DONE')">✅ Выполнена</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

/** Получение бейджа статуса */
function getStatusBadge(status) {
    switch (status) {
        case "TODO":
            return '<span class="badge bg-primary px-2 py-1 fs-6">📌 TODO</span>';
        case "IN_PROGRESS":
            return '<span class="badge bg-warning text-dark px-2 py-1 fs-6">⏳ В работе</span>';
        case "IN_REVIEW":
            return '<span class="badge bg-info text-dark px-2 py-1 fs-6">🧐 На проверке</span>';
        case "DONE":
            return '<span class="badge bg-success px-2 py-1 fs-6">✅ Выполнена</span>';
        default:
            return '<span class="badge bg-light text-dark px-2 py-1 fs-6">❓ Неизвестно</span>';
    }
}

/** Изменение статуса задачи */
function changeTaskStatus(taskId, newStatus) {
    fetchWithAuth(`/api/tasks/${taskId}/status?status=${newStatus}`, {
        method: "PATCH"
    })
        .then(response => {
            if (!response.ok) throw new Error("Не удалось изменить статус");
            return loadTasks();
        })
        .catch(error => console.error("Ошибка при изменении статуса:", error));
}
