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