document.addEventListener("DOMContentLoaded", async function () {
    await loadTeams();
    await loadUsersWithoutTeam();
    setupAssignButtons();
});

/** Загружает список всех команд с участниками */
async function loadTeams() {
    try {
        const response = await fetchWithAuth("/api/teams");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const teams = await response.json();
        displayTeams(teams);
    } catch (error) {
        console.error("Ошибка при загрузке команд:", error);
    }
}

/** Загружает список пользователей без команды */
async function loadUsersWithoutTeam() {
    try {
        const response = await fetchWithAuth("/api/users/list-users-without-team");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const users = await response.json();
        displayUsersWithoutTeam(users);
        await loadUser()
    } catch (error) {
        console.error("Ошибка при загрузке пользователей без команды:", error);
    }
}

/** Отображает пользователей без команды */
function displayUsersWithoutTeam(users) {
    const container = document.getElementById("users-list");
    container.innerHTML = ""; // Очищаем контейнер перед обновлением

    if (users.length === 0) {
        container.innerHTML = "<p class='text-center'>Нет пользователей без команды.</p>";
        return;
    }

    users.forEach(user => {
        const userCard = document.createElement("div");
        userCard.classList.add("card", "bg-dark", "text-light", "border-secondary", "mb-3");
        userCard.innerHTML = `
            <div class="card-body">
                <h5 class="card-title">${user.username} | ${user.email}</h5>
                <p class="card-text">Роль: ${user.role.name} | Уровень доступа: ${user.role.accessLevel}</p>
                <button class="btn btn-primary" data-access-level="HIGH">Добавить в команду</button>
            </div>
        `;
        container.appendChild(userCard);
    });
}

/** Отображает команды с участниками */
function displayTeams(teams) {
    const container = document.getElementById("teams-container");
    container.innerHTML = ""; // Очищаем контейнер перед обновлением

    if (teams.length === 0) {
        container.innerHTML = "<p class='text-center'>Нет доступных команд.</p>";
        return;
    }

    teams.forEach(teamData => {
        const {team, members} = teamData;

        // Карточка команды
        const teamCard = document.createElement("div");
        teamCard.classList.add("col-md-6", "col-lg-4"); // Responsive Grid Bootstrap

        teamCard.innerHTML = `
            <div class="card bg-dark text-light border-secondary">
                <div class="card-body">
                    <h5 class="card-title">${team.name} <span class="text-muted">(ID: ${team.id})</span></h5>
                    <p class="card-text"><strong>Лидер:</strong> ${teamData.leader ? teamData.leader.username : "Нет лидера"}</p>
                    <h6>Участники:</h6>
                    <ul class="list-group list-group-flush">
                        ${members.length > 0
            ? members.map(member => `<li class="list-group-item bg-dark text-light">${member.username} (XP: ${member.xp}, LVL: ${member.lvl})</li>`).join("")
            : `<li class="list-group-item bg-dark text-muted">Нет участников</li>`
        }
                    </ul>
                </div>
            </div>
        `;

        container.appendChild(teamCard);
    });
}

/** Фильтрация списка команд по названию и участникам */
function searchTeams() {
    const query = document.getElementById("search").value.toLowerCase();
    const teamCards = document.querySelectorAll("#teams-container .col-md-6");

    teamCards.forEach(card => {
        const text = card.textContent.toLowerCase();
        card.style.display = text.includes(query) ? "block" : "none";
    });
}

/** Настраивает обработчики для кнопок "Добавить в команду" */
function setupAssignButtons() {
    document.getElementById("users-list").addEventListener("click", async function (event) {
        if (event.target.classList.contains("btn-primary")) {
            const button = event.target;
            const userCard = button.closest(".card-body");
            const username = userCard.querySelector(".card-title").textContent.trim();

            await loadTeamOptions();

            // Запоминаем пользователя, которого добавляем
            document.getElementById("confirmAssign").setAttribute("data-username", username);

            // Открываем модальное окно
            const modal = new bootstrap.Modal(document.getElementById("teamSelectModal"));
            modal.show();
        }
    });

    document.getElementById("confirmAssign").addEventListener("click", async function () {
        const username = this.getAttribute("data-username");
        const teamId = document.getElementById("teamSelect").value;

        if (teamId) {
            await assignToTeam(teamId, username);
            bootstrap.Modal.getInstance(document.getElementById("teamSelectModal")).hide();
        } else {
            alert("Выберите команду!");
        }
    });
}
async function assignToTeam(teamId, username) {
    try {
        const response = await fetchWithAuth(`/api/teams/${teamId}/add-user/${username}`, {
            method: "POST"
        });

        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

        alert("Пользователь успешно добавлен в команду!");
        await loadTeams();
        await loadUsersWithoutTeam();
    } catch (error) {
        console.error("Ошибка при добавлении пользователя в команду:", error);
        alert("Не удалось добавить пользователя в команду.");
    }
}


/** Загружает список команд в модальное окно */
async function loadTeamOptions() {
    try {
        const response = await fetchWithAuth("/api/teams");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const teams = await response.json();

        const select = document.getElementById("teamSelect");
        select.innerHTML = `<option selected disabled>Выберите команду</option>`;

        teams.forEach(team => {
            const option = document.createElement("option");
            option.value = team.team.id;
            option.textContent = team.team.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Ошибка загрузки команд:", error);
    }
}
/** Удаляет пользователя из команды */
async function removeUserFromTeam(teamId, username) {
    if (!confirm(`Удалить пользователя ${username} из команды?`)) return;
    try {
        const response = await fetchWithAuth(`/api/teams/${teamId}/remove-user/${username}`, { method: "DELETE" });
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        alert("Пользователь удален из команды.");
        await loadTeams();
    } catch (error) {
        console.error("Ошибка при удалении пользователя:", error);
        alert("Не удалось удалить пользователя.");
    }
}

/** Удаляет команду */
async function deleteTeam(teamId) {
    if (!confirm("Вы уверены, что хотите удалить эту команду?")) return;
    try {
        const response = await fetchWithAuth(`/api/teams/${teamId}`, { method: "DELETE" });
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        alert("Команда успешно удалена.");
        await loadTeams();
    } catch (error) {
        console.error("Ошибка при удалении команды:", error);
        alert("Не удалось удалить команду.");
    }
}