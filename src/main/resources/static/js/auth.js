document.addEventListener("DOMContentLoaded", function () {
    console.log("Страница загружена");

    const loginForm = document.getElementById("loginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const username = document.getElementById("username").value.trim();
            const password = document.getElementById("password").value.trim();

            if (!username || !password) {
                document.getElementById("errorMessage").innerText = "Введите логин и пароль!";
                return;
            }

            try {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");

                console.log("Отправляем запрос с данными:", {username, password});

                const response = await fetch("/api/auth/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({username, password})
                });

                if (!response.ok) {
                    console.error("Ошибка входа:", response.status, response.statusText);
                    const errorData = await response.json().catch(() => null);
                    document.getElementById("errorMessage").innerText = errorData?.error || "Ошибка входа";
                    return;
                }

                const data = await response.json();
                console.log("Полученные токены:", data);

                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);

                console.log("Авторизация успешна, переходим на /dashboard");
                window.location.href = "/dashboard";

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

    console.log("Выходим, refreshToken:", refreshToken);

    if (refreshToken) {
        try {
            const response = await fetch("/api/auth/logout", {
                method: "POST",
                headers: {"Content-Type": "application/x-www-form-urlencoded"},
                body: new URLSearchParams({refreshToken})
            });

            console.log("Ответ сервера при выходе:", response.status, response.statusText);
        } catch (error) {
            console.error("Ошибка при выходе:", error);
        }
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
