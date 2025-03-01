const allAchievements = [
    {name: "Коврик для мыши", description: "Добился 1 уровня", image: "/img/level-1.png", level: 1},
    {name: "Кружка", description: "Добился 2 уровня", image: "/img/level-2.png", level: 2},
    {name: "Спортивная бутылка", description: "Добился 3 уровня", image: "/img/level-3.png", level: 3},
    {name: "Кепка", description: "Добился 4 уровня", image: "/img/level-4.png", level: 4},
    {name: "Худи", description: "Добился 5 уровня", image: "/img/level-5.png", level: 5},
    {name: "Рюкзак", description: "Добился 6 уровня", image: "/img/level-6.png", level: 6},
];

// Загружаем уровень пользователя и полученные награды
async function loadAchievements() {
    try {
        // Запрос уровня
        const userResponse = await fetchWithAuth("/api/users/me");
        if (!userResponse.ok) throw new Error(`Ошибка: ${userResponse.status}`);
        const user = await userResponse.json();
        const userLevel = user.lvl;
        const username = user.username;

        // Запрос полученных ачивок
        const achievementsResponse = await fetchWithAuth("/api/merch/obtained");
        if (!achievementsResponse.ok) throw new Error(`Ошибка: ${achievementsResponse.status}`);
        const obtainedNames = await achievementsResponse.json();

        // Рендерим карточки с учётом уровня
        renderAchievements(userLevel, obtainedNames, username);
    } catch (error) {
        console.warn("Ошибка загрузки достижений:", error);
    }
}

// Функция рендеринга карточек
function renderAchievements(userLevel, obtainedNames, username) {
    const container = document.getElementById("achievements-container");
    container.innerHTML = ""; // Очищаем перед рендерингом

    allAchievements.forEach(achievement => {
        if (userLevel < achievement.level) return; // Если уровень недостаточен – не показываем награду

        const isObtained = obtainedNames.includes(achievement.name);

        const card = document.createElement("div");
        card.className = "col";
        card.innerHTML = `
            <div class="card bg-dark text-light shadow-lg border-0 rounded-4 h-100">
                <img src="${achievement.image}" class="card-img-top rounded-top-4" alt="${achievement.name}">
                <div class="card-body">
                    <h5 class="card-title text-warning">${achievement.name}</h5>
                    <p class="card-text text-secondary">${achievement.description}</p>
                    ${isObtained
            ? '<span class="badge bg-success">Получено</span>'
            : `<button class="btn btn-outline-warning btn-sm request-award-btn" data-username="${username}" data-achievement="${achievement.name}">Запросить</button>`}
                </div>
            </div>
        `;
        container.appendChild(card);
    });

    // Добавляем обработчики на кнопки "Запросить"
    document.querySelectorAll(".request-award-btn").forEach(button => {
        button.addEventListener("click", async function () {
            const username = this.dataset.username;
            const achievementName = this.dataset.achievement;
            await requestAward(username, achievementName);
        });
    });
}

// Функция запроса награды
async function requestAward(username, achievementName) {
    try {
        const response = await fetchWithAuth(`/api/merch/request-an-administrator-award?username=${username}&achievementName=${achievementName}`, {
            method: "POST"
        });

        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

        alert(`Запрос на награду "${achievementName}" отправлен администратору.`);
        loadAchievements(); // Перезагружаем список наград
    } catch (error) {
        console.warn("Ошибка при запросе награды:", error);
    }
}

// Загружаем достижения при загрузке страницы
document.addEventListener("DOMContentLoaded", loadAchievements);
