document.addEventListener("DOMContentLoaded", async function () {
    try {
        await loadUserProfile();  // Загружаем профиль
        await updateUserLevelInfo();  // Загружаем XP и уровень
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

    // Стили для корректного отображения
    progressBar.style.color = "black"; // Черный текст
    progressBar.style.fontWeight = "bold"; // Жирный шрифт
    progressBar.style.lineHeight = "30px";
}
