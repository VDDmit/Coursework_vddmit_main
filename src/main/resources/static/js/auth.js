document.addEventListener("DOMContentLoaded", function () {
    console.log("Страница загружена");

    const loginForm = document.getElementById("loginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;

            try {
                // Очистим токены перед логином
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

                    let options = {
                        method: "GET",
                        headers: {
                            "Authorization": `Bearer ${data.accessToken}`,
                            "Content-Type": "application/json"
                        }
                    };

                    console.log("Отправляем запрос на /api/users/me с заголовками:", options.headers);
                    const userResponse = await fetch("/api/users/me", options);

                    console.log("Ответ от /api/users/me:", userResponse.status, userResponse.statusText);

                    if (userResponse.ok) {
                        console.log("Авторизация успешна, переходим на /dashboard");
                        window.location.href = "/dashboard";
                    } else {
                        console.error("Ошибка авторизации после логина.");
                        await logout();
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

/** Функция получения accessToken */
async function getAccessToken() {
    let accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
        console.warn("Токен не найден, выход...");
        await logout();
        return null;
    }

    const decoded = parseJwt(accessToken);
    const now = Math.floor(Date.now() / 1000);

    if (decoded.exp < now) {
        console.warn("Токен истёк, выход...");
        await logout();
        return null;
    }

    return accessToken;
}

/** Запрос с авторизацией */
async function fetchWithAuth(url, options = {}) {
    let accessToken = await getAccessToken();
    if (!accessToken) return null;

    options.headers = {
        ...options.headers,
        Authorization: `Bearer ${accessToken}`
    };

    console.log("Отправляем запрос на", url, "с заголовками", options.headers);
    let response = await fetch(url, options);

    if (response.status === 401) {
        console.warn("Получен 401, пробуем заново...");
        accessToken = await getAccessToken();
        if (!accessToken) return null;

        options.headers.Authorization = `Bearer ${accessToken}`;
        response = await fetch(url, options);
    }

    return response;
}

/** Выход */
async function logout() {
    const refreshToken = localStorage.getItem("refreshToken");

    if (refreshToken) {
        await fetch("/api/auth/logout", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({refreshToken})
        });
    }

    localStorage.clear();
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
