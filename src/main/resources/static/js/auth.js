document.addEventListener("DOMContentLoaded", function () {
    console.log("Страница загружена");

    const loginForm = document.getElementById("loginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;

            try {
                // Очистим localStorage перед новой попыткой входа
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");

                const response = await fetch("/api/auth/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: new URLSearchParams({username, password})
                });

                const data = await response.json();

                if (response.ok) {
                    localStorage.setItem("accessToken", data.accessToken);
                    localStorage.setItem("refreshToken", data.refreshToken);
                    console.log("Сохранили токены:", data.accessToken, data.refreshToken);

                    // После входа проверяем, действителен ли токен
                    const userResponse = await fetchWithAuth("/api/users/me");
                    if (userResponse.ok) {
                        console.log("Авторизация успешна, загружаем /dashboard");

                        // Запрашиваем страницу с токеном
                        fetchWithAuth("/dashboard")
                            .then(response => response.text())
                            .then(html => {
                                document.open();
                                document.write(html);
                                document.close();
                            });
                    } else {
                        console.error("Ошибка авторизации после логина.");
                        window.location.href = "/login";
                    }

                } else {
                    console.error("Ошибка входа:", data);
                    document.getElementById("errorMessage").innerText = data.error || "Ошибка входа";
                }
            } catch (error) {
                console.error("Ошибка входа:", error);
                document.getElementById("errorMessage").innerText = "Ошибка авторизации. Попробуйте ещё раз.";
            }
        });
    }
});


/** Функция для получения accessToken, обновляя его при необходимости */
async function getAccessToken() {
    let accessToken = localStorage.getItem("accessToken");
    console.log("Получаем accessToken:", accessToken);

    if (!accessToken) {
        console.log("Токен не найден, вызываем logout()");
        logout();
        return null;
    }

    const decoded = parseJwt(accessToken);
    const now = Math.floor(Date.now() / 1000);
    console.log("Время истечения токена:", decoded.exp, "Текущее время:", now);

    if (decoded.exp < now) {
        console.log("Токен истёк, вызываем logout()");
        logout();
    }

    return accessToken;
}

/** Перехват 401 и обновление токена */
async function fetchWithAuth(url, options = {}) {
    let accessToken = await getAccessToken();
    if (!accessToken) {
        logout();
        return;
    }

    options.headers = options.headers || {};
    options.headers.Authorization = `Bearer ${accessToken}`;

    let response = await fetch(url, options);

    if (response.status === 401) {
        accessToken = await getAccessToken();
        if (!accessToken) {
            logout();
            return;
        }

        options.headers.Authorization = `Bearer ${accessToken}`;
        response = await fetch(url, options);
    }

    return response;
}

/** Выход из системы */
async function logout() {
    const refreshToken = localStorage.getItem("refreshToken");

    if (refreshToken) {
        await fetch("/api/auth/logout", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({refreshToken})
        });
    }

    localStorage.removeItem("refreshToken");
    window.location.href = "/login";
}

/** Декодирование JWT */
function parseJwt(token) {
    try {
        return JSON.parse(atob(token.split(".")[1]));
    } catch (e) {
        return {};
    }
}
