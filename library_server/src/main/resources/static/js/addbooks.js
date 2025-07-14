// Show flash messages from sessionStorage/localStorage on Add Books page

document.addEventListener('DOMContentLoaded', function() {
    const successMessage = sessionStorage.getItem("success");
    const errorMessage = sessionStorage.getItem("error");
    const flashMessages = document.getElementById("flashMessages");

    if (successMessage) {
        flashMessages.innerHTML += `<li class="success">${successMessage}</li>`;
        sessionStorage.removeItem("success");
    }

    if (errorMessage) {
        flashMessages.innerHTML += `<li class="error">${errorMessage}</li>`;
        sessionStorage.removeItem("error");
    }
});

if (window.location.href.indexOf("add_books") !== -1) {
    if (window.localStorage.getItem("flashSuccess")) {
        sessionStorage.setItem("success", window.localStorage.getItem("flashSuccess"));
        window.localStorage.removeItem("flashSuccess");
    }
    if (window.localStorage.getItem("flashError")) {
        sessionStorage.setItem("error", window.localStorage.getItem("flashError"));
        window.localStorage.removeItem("flashError");
    }
} 