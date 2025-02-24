document.addEventListener("DOMContentLoaded", async function () {
    await loadProjects();
    initializeSwiper(); // Инициализация Swiper после загрузки проектов
});

async function loadProjects() {
    try {
        const response = await fetchWithAuth("/api/projects/with-members");
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const projects = await response.json();
        displayProjects(projects);
    } catch (error) {
        console.error("Ошибка при загрузке проектов:", error);
    }
}

function displayProjects(projects) {
    const container = document.getElementById("projects-container");
    container.innerHTML = projects.length ? "" : "<p class='text-center'>Нет доступных проектов.</p>";

    projects.forEach(project => {
        const slide = document.createElement('div');
        slide.classList.add('swiper-slide');

        slide.innerHTML = `
            <div class="project-card card p-4">
                <h5>${project.name}</h5>
                <p>${project.description}</p>
                <strong>Участники:</strong>
                <ul>
                    ${project.members.map(member => `<li>${member.username}</li>`).join("")}
                </ul>
            </div>
        `;

        container.appendChild(slide);
    });
}

function initializeSwiper() {
    if (typeof Swiper === "undefined") {
        console.error("Swiper не загружен!");
        setTimeout(initializeSwiper, 100); // Повторная попытка через 100 мс
        return;
    }
    console.log("Swiper инициализируется...");
    new Swiper(".mySwiper", {
        loop: true,
        navigation: {
            nextEl: ".swiper-button-next",
            prevEl: ".swiper-button-prev",
        },
        pagination: {
            el: ".swiper-pagination",
            clickable: true,
        },
        autoplay: {
            delay: 10000,
            disableOnInteraction: false,
        },
        spaceBetween: 20,
        slidesPerView: 1,
        breakpoints: {
            768: {slidesPerView: 2},
            1024: {slidesPerView: 3}
        }
    });
}