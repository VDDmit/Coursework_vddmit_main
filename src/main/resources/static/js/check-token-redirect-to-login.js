document.addEventListener("DOMContentLoaded", async function () {
    console.log("Проверяем аутентификацию...");

    checkAuth().then(isAuthenticated => {
        if (!isAuthenticated) {
            console.warn("Пользователь не авторизован, редирект на /login");
            window.location.href = "/login";
        } else {
            console.log("Пользователь авторизован, загружаем интерфейс...");
            loadUser(); // Загружаем юзера только после проверки авторизации
        }
    });
});

/** Проверка, авторизован ли пользователь */
async function checkAuth() {
    let accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
        console.warn("AccessToken отсутствует");
        return false;
    }

    const decoded = parseJwt(accessToken);
    const now = Math.floor(Date.now() / 1000);

    if (decoded.exp < now) {
        console.warn("AccessToken истёк");
        return false;
    }

    return true;
}
