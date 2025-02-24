document.addEventListener("DOMContentLoaded", async function () {
    await initializeApp();
});

async function initializeApp() {
    await loadTeams();
    await loadTopTeams();
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
    document.querySelectorAll(".delete-btn-for-admin").forEach(btn => {
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
                <div class="d-flex justify-content-between align-items-center">
                    <h5 class="card-title mb-0">
                        ${teamData.team.name} <span class="text-muted">(ID: ${teamData.team.id})</span>
                    </h5>
                    <button class="delete-btn-for-admin btn btn-danger btn-sm hidden" onclick="deleteTeam('${teamData.team.id}')">Удалить команду</button>
                </div>
                <p class="card-text"><strong>Лидер:</strong> ${teamData.leader ? teamData.leader.username : "Нет лидера"}</p>
                <h6>Участники:</h6>
                <ul class="list-group list-group-flush">
                    ${teamData.members.map(member =>
        `<li class="list-group-item bg-dark text-light d-flex justify-content-between align-items-center" id="user-${member.id}">
                        <span>${member.username} (XP: ${member.xp}, LVL: ${member.lvl})</span>
                        <button class="delete-btn-for-admin btn btn-danger btn-sm hidden p-0 d-flex align-items-center justify-content-center" 
                                style="width: 20px; height: 20px; border-radius: 4px;" 
                                onclick="removeUserFromTeam('${teamData.team.id}', '${member.username}')">
                            <span style="font-size: 12px;">-</span>
                        </button>
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

function searchWithoutTeamUsers() {
    const query = document.getElementById("search-without-team-user").value.toLowerCase();
    const userCards = document.querySelectorAll("#users-list .card");

    userCards.forEach(card => {
        const cardText = card.textContent.toLowerCase();
        if (cardText.includes(query)) {
            card.style.display = "block";
        } else {
            card.style.display = "none";
        }
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

async function loadTopTeams() {
    try {
        const response = await fetchWithAuth("/api/teams/top_teams/5");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const topTeams = await response.json();
        displayTopTeamsChart(topTeams);
    } catch (error) {
        console.error("Ошибка при загрузке топ-команд:", error);
    }
}

function displayTopTeamsChart(teams) {
    const ctx = document.getElementById("topTeamsChart").getContext("2d");

    if (window.topTeamsChartInstance) {
        window.topTeamsChartInstance.destroy();
    }

    const labels = teams.map(team => team.team.name);
    const teamXP = teams.map(team => team.members.reduce((sum, member) => sum + member.xp, team.leader.xp));
    const leaderXP = teams.map(team => team.leader.xp);

    window.topTeamsChartInstance = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [
                {
                    type: "bar",
                    label: "XP лидера",
                    data: leaderXP,
                    backgroundColor: "rgba(255, 99, 132, 0.7)",
                    borderColor: "rgba(255, 99, 132, 1)",
                    borderWidth: 1
                },
                {
                    type: "line",
                    label: "Общий XP команды",
                    data: teamXP,
                    borderColor: "rgba(54, 162, 235, 1)",
                    backgroundColor: "rgba(54, 162, 235, 0.2)",
                    borderWidth: 2,
                    pointRadius: 5,
                    pointBackgroundColor: "rgba(54, 162, 235, 1)",
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function (tooltipItem) {
                            return `${tooltipItem.dataset.label}: ${tooltipItem.raw} XP`;
                        }
                    }
                }
            }
        }
    });
}
