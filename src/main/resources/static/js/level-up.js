document.addEventListener("DOMContentLoaded", async function () {
    await checkLevelUp();
});

async function checkLevelUp() {
    try {
        const user = await getUserInfo(); // Получаем данные пользователя
        if (!user) return;

        let previousLevel = localStorage.getItem("userLevel") || 0;
        let currentLevel = user.lvl; // Текущий уровень из API

        if (currentLevel > previousLevel) {
            showLevelUpModal(currentLevel);
            localStorage.setItem("userLevel", currentLevel);
        }
    } catch (error) {
        console.warn("Ошибка проверки уровня пользователя:", error);
    }
}

async function getUserInfo() {
    try {
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.warn("Ошибка получения информации о пользователе:", error);
        return null;
    }
}

function showLevelUpModal(level) {
    document.getElementById("newLevel").textContent = level;
    let modal = new bootstrap.Modal(document.getElementById("levelUpModal"));
    modal.show();
}
