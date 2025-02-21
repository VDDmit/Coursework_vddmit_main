// Открывает модальное окно регистрации
function openRegisterModal() {
    let registerModal = new bootstrap.Modal(document.getElementById('registerModal'));
    registerModal.show();
}

// Функция регистрации пользователя
async function registerUser() {
    const username = document.getElementById("reg-username").value.trim();
    const email = document.getElementById("reg-email").value.trim();
    const password = document.getElementById("reg-password").value.trim();
    const role = document.getElementById("reg-role").value;
    const errorDiv = document.getElementById("reg-error");

    if (!username || !email || !password) {
        errorDiv.textContent = "Заполните все поля!";
        return;
    }

    try {
        const response = await fetchWithAuth("/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                username: username,
                email: email,
                password: password,
                roleName: role
            })
        });

        const result = await response.text();

        if (response.ok) {
            alert("Пользователь зарегистрирован!");
            document.getElementById("registerModal").querySelector(".btn-close").click();
        } else {
            errorDiv.textContent = "Ошибка: " + result;
        }
    } catch (error) {
        errorDiv.textContent = "Ошибка при регистрации";
        console.error("Ошибка при регистрации:", error);
    }
}
