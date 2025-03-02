document.addEventListener("DOMContentLoaded", async function () {
    try {
        await loadUserProfile();  // Загружаем профиль
        await updateUserLevelInfo();  // Загружаем XP и уровень
        await loadRankingChart();  // Загружаем рейтинг
    } catch (error) {
        console.error("Ошибка при загрузке данных:", error);
        logout();
    }
});

// Функция загрузки профиля пользователя
async function loadUserProfile() {
    try {
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();

        // Обновляем данные в профиле
        document.getElementById("profileUsername").textContent = `Имя: ${user.username}`;
        document.getElementById("profileId").textContent = `ID: ${user.id}`;
        document.getElementById("profileEmail").textContent = `Email: ${user.email}`;
        document.getElementById("profileTeam").textContent = `Команда: ${user.team ? user.team.name : "Нет команды"}`;
        document.getElementById("profileRole").textContent = `Роль: ${user.role.name}; уровень доступа: ${user.role.accessLevel}`;

        // Обновляем аватар, если у пользователя есть кастомный
        if (user.avatarUrl) {
            document.getElementById("profileAvatar").src = user.avatarUrl;
        }

    } catch (error) {
        console.error("Ошибка загрузки профиля:", error);
    }
}

// Функция обновления информации об уровне и XP пользователя
async function updateUserLevelInfo() {
    try {
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();

        // Обновляем данные на странице
        document.getElementById("levelText").textContent = `Уровень: ${user.lvl}`;
        document.getElementById("xpText").textContent = `XP: ${user.xp}`;
        document.getElementById("nextLevelText").textContent = `До след. уровня: ${user.lvl * 1000 - user.xp} XP`;

        // Обновляем прогресс-бар
        updateXPProgressBar(user.xp, user.lvl * 1000, user.lvl);

    } catch (error) {
        console.warn("Ошибка загрузки информации о пользователе:", error);
    }
}


// Функция обновления прогресс-бара опыта
function updateXPProgressBar(currentXP, nextLevelXP, level) {
    const progressBar = document.getElementById("xpProgressBar");

    // Опыт в начале текущего уровня
    const levelStartXP = (level - 1) * 1000;
    // Опыт, необходимый для перехода на следующий уровень
    const xpForCurrentLevel = nextLevelXP - levelStartXP;
    // Текущий прогресс в рамках уровня
    const xpInCurrentLevel = currentXP - levelStartXP;
    // Рассчитываем процент прогресса
    const percentage = (xpInCurrentLevel / xpForCurrentLevel) * 100;

    progressBar.style.width = `${percentage.toFixed(2)}%`;
    progressBar.textContent = `${percentage.toFixed(2)}%`;
}


// Функция загрузки рейтинга пользователей
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
    if (window.rankingChart instanceof Chart) {
        window.rankingChart.destroy();
    }

    window.rankingChart = new Chart(ctx, {
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
                        color: "#fff"
                    }
                },
                y: {
                    ticks: {
                        color: "#fff"
                    },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    labels: {
                        color: "#fff"
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
