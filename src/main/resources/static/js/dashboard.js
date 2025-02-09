document.addEventListener("DOMContentLoaded", async function () {
    try {
        // Получаем данные о пользователе
        const userResponse = await fetchWithAuth("/api/users/me", {method: "GET"});
        if (userResponse.ok) {
            const user = await userResponse.json();
            document.getElementById("username").textContent = user.username;
        } else {
            console.warn("Ошибка получения пользователя:", userResponse.status);
            logout();
        }

        // Загружаем список задач
        const tasksResponse = await fetchWithAuth("/api/tasks/list", {method: "GET"});
        if (tasksResponse.ok) {
            const tasks = await tasksResponse.json();
            console.log("Задачи загружены:", tasks);
            renderTasks(tasks);
        } else {
            console.warn("Ошибка загрузки задач:", tasksResponse.status);
        }

    } catch (error) {
        console.error("Ошибка при загрузке данных:", error);
        logout();
    }
});

function renderTasks(tasks) {
    const taskContainer = document.querySelector(".list-group");
    taskContainer.innerHTML = ""; // Очищаем список перед добавлением новых задач

    if (tasks.length === 0) {
        taskContainer.innerHTML = `<p class="text-secondary">У вас нет задач.</p>`;
        return;
    }

    tasks.forEach(task => {
        const taskItem = document.createElement("div");
        taskItem.classList.add("list-group-item", "d-flex", "justify-content-between", "align-items-center", "bg-dark", "text-light");

        taskItem.innerHTML = `
                <div>
                    <h5>${task.title}</h5>
                    <p class="small text-secondary">${task.description}</p>
                </div>
                <div>
                    <a href="/tasks/edit/${task.id}" class="btn btn-warning btn-sm">Редактировать</a>
                    <a href="/tasks/delete/${task.id}" class="btn btn-danger btn-sm">Удалить</a>
                </div>
            `;
        taskContainer.appendChild(taskItem);
    });
}

