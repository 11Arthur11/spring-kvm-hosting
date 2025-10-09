import { errorMessages } from './errors.js';
const alertBox = document.getElementById('alertBox');
const loginSubmit = document.getElementById('loginSubmit');
const registerSubmit = document.getElementById('registerSubmit');


loginSubmit.addEventListener('click', function(ev) {
    ev.preventDefault();
    login()
})

registerSubmit.addEventListener('click', function(ev) {
    ev.preventDefault();
    register()
})

function showAlert(msg) {
    document.getElementById("alertBody").textContent = msg;
    alertBox.classList.add('active');
}

function login() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const rememberMe = document.getElementById('rememberMe').checked;

    fetch('/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({email, password, rememberMe}),
    }).then(async response => {
        if (response.ok) {
            window.location.href = '/';
        } else {
            const errorKey = await response.text();
            const message = errorMessages[errorKey] || errorMessages["BAD_REQUEST"];
            showAlert(message);
        }
    })
}

function register() {
    const email = document.getElementById('email').value.trim();
    const fullName = document.getElementById('fullName').value.trim();
    const rawPassword = document.getElementById('rawPassword').value.trim();
    const rawPasswordConfirm = document.getElementById('rawPasswordConfirm').value.trim();

    fetch('/api/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({email, fullName, rawPassword, rawPasswordConfirm})
    }).then(async response => {
        if (response.ok) {
            window.location.href = '/';
        } else  {
            const errorKey = await response.text();
            const message = errorMessages[errorKey] || errorMessages["BAD_REQUEST"];
            showAlert(message);
        }
    })
}