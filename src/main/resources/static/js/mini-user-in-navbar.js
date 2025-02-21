/** Загружает уровень доступа текущего пользователя и ися в мини-профиль */
async function loadUser() {
    try {
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();

        // Определяем уровень доступа
        const accessLevels = ["LOW", "MEDIUM", "HIGH"];
        const userAccessLevel = user.role.accessLevel; // "HIGH", "MEDIUM" или "LOW"
        const userAccessIndex = accessLevels.indexOf(userAccessLevel);

        // Скрываем кнопки, если у пользователя недостаточно прав
        document.querySelectorAll("[data-access-level]").forEach(button => {
            const requiredAccess = button.getAttribute("data-access-level");
            const requiredIndex = accessLevels.indexOf(requiredAccess);
            if (userAccessIndex < requiredIndex) {
                button.style.display = "none";
            }
        });

        // Обновляем имя пользователя в шапке
        document.getElementById("username").textContent = user.username;
    } catch (error) {
        console.error("Ошибка при загрузке данных пользователя:", error);
    }
}

