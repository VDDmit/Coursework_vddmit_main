document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;

            try {
                const response = await fetch("/api/auth/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: new URLSearchParams({ username, password })
                });

                const data = await response.json();

                if (response.ok) {
                    localStorage.setItem("accessToken", data.accessToken);
                    localStorage.setItem("refreshToken", data.refreshToken);
                    console.log("Токены сохранены, перенаправление...");
                    window.location.href = "/dashboard"; // Перенаправление
                } else {
                    document.getElementById("errorMessage").innerText = data.error || "Ошибка входа";
                }
            } catch (error) {
                console.error("Ошибка входа:", error);
            }
        });
    }
});

/** Функция для получения accessToken, обновляя его при необходимости */
async function getAccessToken() {
    let accessToken = localStorage.getItem("accessToken");

    if (!accessToken) return null;

    // Проверяем, не истёк ли токен
    const decoded = parseJwt(accessToken);
    const now = Math.floor(Date.now() / 1000);

    if (decoded.exp < now) {
        try {
            const refreshToken = localStorage.getItem("refreshToken");
            const response = await fetch("/api/auth/refresh", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ refreshToken })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem("accessToken", data.accessToken);
                return data.accessToken;
            } else {
                logout();
            }
        } catch (error) {
            console.error("Ошибка обновления токена:", error);
            logout();
        }
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
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ refreshToken })
        });
    }

    localStorage.removeItem("accessToken");
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
