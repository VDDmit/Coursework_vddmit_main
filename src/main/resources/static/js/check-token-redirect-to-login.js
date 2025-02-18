document.addEventListener("DOMContentLoaded", async function () {
    console.log("Проверяем аутентификацию...");

    const isAuthenticated = await checkAuth();
    if (!isAuthenticated) {
        console.warn("Пользователь не авторизован, редирект на /login");
        window.location.href = "/login";
    }
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
