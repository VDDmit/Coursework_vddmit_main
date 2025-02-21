document.addEventListener("DOMContentLoaded", async function () {
    await loadTeams();
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

/** Отображает команды с участниками */
function displayTeams(teams) {
    const container = document.getElementById("teams-container");
    container.innerHTML = ""; // Очищаем контейнер перед обновлением

    if (teams.length === 0) {
        container.innerHTML = "<p class='text-center'>Нет доступных команд.</p>";
        return;
    }

    teams.forEach(teamData => {
        const { team, members } = teamData;

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
