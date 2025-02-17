document.addEventListener("DOMContentLoaded", async function () {
    try {
        await loadUser();
        await updateUserLevelInfo();
        await loadTasks();
        initializeSearch(); // Инициализируем поиск после загрузки задач
    } catch (error) {
        console.error("Ошибка при загрузке данных:", error);
        logout();
    }
});

// Загрузка данных о пользователе (имя)
async function loadUser() {
    try {
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();
        document.getElementById("username").textContent = user.username;
    } catch (error) {
        console.warn("Ошибка получения пользователя:", error);
        logout();
    }
}

async function updateUserLevelInfo() {
    try {
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();

        // Обновляем данные на странице
        document.getElementById("levelText").textContent = `Уровень: ${user.lvl}`;
        document.getElementById("xpText").textContent = `XP: ${user.xp}`;
        document.getElementById("nextLevelText").textContent = `До след. уровня: ${user.lvl * 1000 - user.xp} XP`;

    } catch (error) {
        console.warn("Ошибка загрузки информации о пользователе:", error);
    }
}


// Функция обновления XP и уровня пользователя
async function updateUserXP(xpAmount) {
    try {
        const response = await fetchWithAuth(`/api/users/update-xp?xp=${xpAmount}`, { method: "POST" });
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();

        // Обновляем блок с информацией о уровне
        document.getElementById("levelInfo").innerHTML =
            `Уровень: ${user.lvl} | XP: ${user.xp} | До след. уровня: ${user.lvl * 1000 - user.xp} XP`;

    } catch (error) {
        console.warn("Ошибка обновления XP:", error);
    }
}

// Глобальный массив для хранения задач
let tasks = [];

// Загрузка списка задач
async function loadTasks() {
    try {
        const response = await fetchWithAuth("/api/tasks/list");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        tasks = await response.json();
        renderTasks(tasks);
    } catch (error) {
        console.warn("Ошибка загрузки задач:", error);
    }
}

// Функция сортировки задач
function sortTasks(type) {
    if (type === "completed") {
        // Сначала выполненные задачи
        tasks.sort((a, b) => b.completed - a.completed);
    } else if (type === "incomplete") {
        // Сначала невыполненные задачи
        tasks.sort((a, b) => a.completed - b.completed);
    }
    renderTasks(tasks);
}

// Инициализация поиска по задачам
function initializeSearch() {
    const searchInput = document.getElementById("searchInput");
    searchInput.addEventListener("input", function () {
        const query = this.value.trim().toLowerCase();
        const filteredTasks = tasks.filter(task => {
            const titleMatch = task.title.toLowerCase().includes(query);
            const descriptionMatch = task.description && task.description.toLowerCase().includes(query);
            return titleMatch || descriptionMatch;
        });
        renderTasks(filteredTasks);
    });
}

// Отображение списка задач с полной информацией
function renderTasks(tasksArray) {
    const taskContainer = document.getElementById("taskList");
    taskContainer.innerHTML = ""; // Очистка контейнера

    if (!tasksArray.length) {
        taskContainer.innerHTML = `<p class="text-secondary">У вас нет задач.</p>`;
        return;
    }

    tasksArray.forEach(task => {
        const taskItem = document.createElement("a");
        taskItem.href = `/tasks/${task.id}`;
        taskItem.classList.add(
            "list-group-item",
            "list-group-item-action",
            "bg-dark",
            "text-light",
            "d-flex",
            "flex-column",
            "hover-effect"
        );

        // Определяем статус задачи
        const status = task.completed
            ? '<span class="badge bg-success px-2 py-1">✅ Выполнена</span>'
            : '<span class="badge bg-warning text-dark px-2 py-1">⏳ В работе</span>';

        // Обрезка длинного описания
        const description = task.description && task.description.length > 50
            ? task.description.slice(0, 50) + "..."
            : (task.description || "Без описания");

        // Вывод XP, проекта и назначенного пользователя с проверкой на null
        const xp = task.xp !== null ? `🎖️ XP: ${task.xp}` : "🎖️ XP: 0";
        const projectTitle = task.project && task.project.name
            ? `📌 Проект: ${task.project.name}`
            : "📌 Проект: Не указан";
        const assignedUser = task.assignedUser && task.assignedUser.username
            ? `👤 ${task.assignedUser.username}`
            : "👤 Не назначен";

        // Формируем HTML задачи
        taskItem.innerHTML = `
            <div class="d-flex justify-content-between w-100">
                <h5 class="mb-1">${task.title}</h5>
                <small>${status}</small>
            </div>
            <p class="mb-1 small text-secondary">${description}</p>
            <div class="d-flex justify-content-between text-muted small">
                <span class="text-light">${xp}</span>
                <span class="text-light">${projectTitle}</span>
                <span class="text-light">${assignedUser}</span>
            </div>
        `;
        taskContainer.appendChild(taskItem);
    });
}

// Глобальная переменная для хранения графика
let rankingChart = null;

// Функция загрузки данных для диаграммы рейтинга
async function loadRankingChart() {
    try {
        const response = await fetchWithAuth("/api/users/me_in_top_list");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

        const topUsers = await response.json();

        // Преобразуем данные для диаграммы
        const labels = topUsers.map(user => user.user.username);
        const xpData = topUsers.map(user => user.user.xp);
        const ranks = topUsers.map(user => `#${user.rank}`);

        // Отрисовываем диаграмму
        renderRankingChart(labels, xpData, ranks);

    } catch (error) {
        console.error("Ошибка загрузки рейтинга:", error);
    }
}

// Функция отрисовки диаграммы с рейтингом
function renderRankingChart(labels, xpData, ranks) {
    const ctx = document.getElementById("rankingChart").getContext("2d");

    // Удаляем предыдущий график, если он уже существует
    if (rankingChart instanceof Chart) {
        rankingChart.destroy();
    }

    rankingChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "XP",
                data: xpData,
                backgroundColor: labels.map(name =>
                    name === document.getElementById("username").textContent ? "#ffcc00" : "#007bff"
                ),
                borderColor: "#fff",
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    ticks: {
                        color: "black" // Цвет чисел на оси X
                    }
                },
                y: {
                    ticks: {
                        color: "black" // Цвет чисел на оси Y
                    },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    labels: {
                        color: "black" // Цвет текста легенды
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${ranks[context.dataIndex]} - ${context.raw} XP`;
                        }
                    }
                }
            }
        }
    });
}

// Загружаем диаграмму при загрузке страницы
document.addEventListener("DOMContentLoaded", loadRankingChart);