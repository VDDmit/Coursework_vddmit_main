document.addEventListener("DOMContentLoaded", async function () {
    await initializeApp();
});

async function initializeApp() {
    await loadTeams();
    await loadUsersWithoutTeam();
    setupEventListeners();
}

/**Функции для загрузки данных*/
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

async function loadUsersWithoutTeam() {
    try {
        const response = await fetchWithAuth("/api/users/list-users-without-team");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const users = await response.json();
        displayUsersWithoutTeam(users);
    } catch (error) {
        console.error("Ошибка при загрузке пользователей без команды:", error);
    }
}

/**Функции для отображения данных*/
function displayTeams(teams) {
    const container = document.getElementById("teams-container");
    container.innerHTML = teams.length ? "" : "<p class='text-center'>Нет доступных команд.</p>";
    teams.forEach(teamData => {
        container.appendChild(createTeamCard(teamData));
    });
}

function displayUsersWithoutTeam(users) {
    const container = document.getElementById("users-list");
    const selectElement = document.getElementById("team-leader");
    container.innerHTML = users.length ? '' : "<p class='text-center'>Нет пользователей без команды.</p>";
    selectElement.innerHTML = '<option selected disabled>Выберите лидера</option>';
    users.forEach(user => {
        container.appendChild(createUserCard(user));
        selectElement.appendChild(createLeaderOption(user));
    });
}

let editMode = false;

function editTeams() {
    editMode = !editMode; // Переключаем режим редактирования
    document.querySelectorAll(".delete-user-btn").forEach(btn => {
        btn.classList.toggle("hidden", !editMode); // Показываем/скрываем кнопки удаления
    });
}


/**Вспомогательные функции для создания элементов DOM*/
function createTeamCard(teamData) {
    const card = document.createElement("div");
    card.classList.add("col-md-6", "col-lg-4");
    card.innerHTML = `
        <div class="card bg-dark text-light border-secondary">
            <div class="card-body">
                <h5 class="card-title">${teamData.team.name} <span class="text-muted">(ID: ${teamData.team.id})</span></h5>
                <p class="card-text"><strong>Лидер:</strong> ${teamData.leader ? teamData.leader.username : "Нет лидера"}</p>
                <h6>Участники:</h6>
                <ul class="list-group list-group-flush">
                    ${teamData.members.map(member =>
        `<li class="list-group-item bg-dark text-light" id="user-${member.id}">
                        ${member.username} (XP: ${member.xp}, LVL: ${member.lvl})
                        <button class="delete-user-btn btn btn-danger btn-sm hidden" onclick="removeUserFromTeam('${teamData.team.id}', '${member.username}')">Удалить</button>
                    </li>`)
        .join("") || `<li class="list-group-item bg-dark text-muted">Нет участников</li>`}
                </ul>
            </div>
        </div>`;
    return card;
}

function createUserCard(user) {
    const card = document.createElement("div");
    card.classList.add("card", "bg-dark", "text-light", "border-secondary", "mb-3");
    card.innerHTML = `
        <div class="card-body">
            <h5 class="card-title">${user.username}</h5>
            <p class="card-text">Роль: ${user.role.name} | Уровень доступа: ${user.role.accessLevel} | ${user.email}</p>
            <button class="btn btn-primary" data-access-level="HIGH">Добавить в команду</button>
        </div>`;
    return card;
}

function createLeaderOption(user) {
    const option = document.createElement("option");
    option.value = user.id;
    option.textContent = user.username;
    option.dataset.username = user.username;
    return option;
}

/**Настройка обработчиков событий*/
function setupEventListeners() {
    document.querySelector("#admin-buttons button.btn-primary").addEventListener("click", showCreateTeamModal);
    document.getElementById("users-list").addEventListener("click", handleAssignButtonClick);
    document.getElementById("confirmAssign").addEventListener("click", handleConfirmAssign);
}

function showCreateTeamModal() {
    const modal = new bootstrap.Modal(document.getElementById("createTeamModal"));
    modal.show();
}

async function handleAssignButtonClick(event) {
    if (event.target.classList.contains("btn-primary")) {
        await loadTeamOptions();
        const username = event.target.closest(".card-body").querySelector(".card-title").textContent.trim();
        document.getElementById("confirmAssign").setAttribute("data-username", username);
        new bootstrap.Modal(document.getElementById("teamSelectModal")).show();
    }
}

async function handleConfirmAssign() {
    const username = this.getAttribute("data-username");
    const teamId = document.getElementById("teamSelect").value;
    if (teamId) {
        await assignToTeam(teamId, username);
        bootstrap.Modal.getInstance(document.getElementById("teamSelectModal")).hide();
    } else {
        alert("Выберите команду!");
    }
}

/**Функции для работы с API*/
async function assignToTeam(teamId, username) {
    try {
        const response = await fetchWithAuth(`/api/teams/${teamId}/add-user/${username}`, {method: "POST"});
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        alert("Пользователь успешно добавлен в команду!");
        await loadTeams();
        await loadUsersWithoutTeam();
    } catch (error) {
        console.error("Ошибка при добавлении пользователя в команду:", error);
        alert("Не удалось добавить пользователя в команду.");
    }
}

async function loadTeamOptions() {
    try {
        const response = await fetchWithAuth("/api/teams");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const teams = await response.json();
        const select = document.getElementById("teamSelect");
        select.innerHTML = `<option selected disabled>Выберите команду</option>`;
        teams.forEach(team => {
            select.appendChild(createTeamOption(team));
        });
    } catch (error) {
        console.error("Ошибка загрузки команд:", error);
    }
}

function createTeamOption(team) {
    const option = document.createElement("option");
    option.value = team.team.id;
    option.textContent = team.team.name;
    return option;
}

/**Дополнительные функции*/
function searchTeams() {
    const query = document.getElementById("search").value.toLowerCase();
    document.querySelectorAll("#teams-container .col-md-6").forEach(card => {
        card.style.display = card.textContent.toLowerCase().includes(query) ? "block" : "none";
    });
}

async function removeUserFromTeam(teamId, username) {
    if (!confirm(`Удалить пользователя ${username} из команды?`)) return;
    try {
        const response = await fetchWithAuth(`/api/teams/${teamId}/remove-user/${username}`, {method: "DELETE"});
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        alert("Пользователь удален из команды.");
        await loadTeams();
    } catch (error) {
        console.error("Ошибка при удалении пользователя:", error);
        alert("Не удалось удалить пользователя.");
    }
}

async function deleteTeam(teamId) {
    if (!confirm("Вы уверены, что хотите удалить эту команду?")) return;
    try {
        const response = await fetchWithAuth(`/api/teams/${teamId}`, {method: "DELETE"});
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        alert("Команда успешно удалена.");
        await loadTeams();
    } catch (error) {
        console.error("Ошибка при удалении команды:", error);
        alert("Не удалось удалить команду.");
    }
}

function createTeam() {
    const teamName = document.getElementById("team-name").value.trim();
    const teamLeaderId = document.getElementById("team-leader").value;
    const errorDiv = document.getElementById("team-error");
    errorDiv.textContent = "";

    if (!teamName || !teamLeaderId) {
        errorDiv.textContent = "Заполните все поля.";
        return;
    }

    fetchWithAuth("/api/teams", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({name: teamName, leaderId: teamLeaderId})
    })
        .then(response => response.ok ? response.json() : Promise.reject(response))
        .then(data => {
            alert("Команда успешно создана!");
            window.location.reload();
        })
        .catch(error => {
            errorDiv.textContent = error.message || "Ошибка создания команды";
        });
}