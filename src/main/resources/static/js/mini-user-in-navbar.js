/**
 * Загружает данные текущего пользователя, обновляет мини-профиль и проверяет уровень доступа для скрытия кнопок.
 */
async function loadUser() {
    try {
        // Загружаем данные текущего пользователя
        const response = await fetchWithAuth("/api/users/me");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const user = await response.json();

        // Обновляем мини-профиль в навбаре
        document.getElementById("username").textContent = user.username;
        if (user.avatarUrl) {
            document.getElementById("userAvatar").src = user.avatarUrl;
        }

        // Получаем уровень доступа пользователя
        const userAccessLevel = user.role.accessLevel.trim().toUpperCase(); // Приводим к верхнему регистру

        console.log("Уровень доступа пользователя:", userAccessLevel); // Отладка

        // Проверяем уровень доступа для всех элементов с атрибутом data-access-level
        document.querySelectorAll("[data-access-level]").forEach(button => {
            const requiredLevels = button.getAttribute("data-access-level")
                .split(",") // Разделяем уровни доступа, если их несколько
                .map(level => level.trim().toUpperCase()); // Приводим к верхнему регистру

            console.log(`Кнопка: ${button.textContent}, Требуемые уровни: ${requiredLevels.join(", ")}`);

            // Если уровень пользователя не соответствует ни одному из требуемых, скрываем кнопку
            if (!requiredLevels.includes(userAccessLevel)) {
                button.style.display = "none";
            }
        });
    } catch (error) {
        console.error("Ошибка при загрузке данных пользователя или проверке уровня доступа:", error);
    }
}
