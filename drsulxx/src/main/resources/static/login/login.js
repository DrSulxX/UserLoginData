document.getElementById('loginForm').addEventListener('submit', function (event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const loginData = {
        username: username,
        password: password,
    };

    // Only include CAPTCHA response if the CAPTCHA container is visible
    if (document.getElementById('captchaContainer').style.display !== 'none') {
        const captchaInput = document.getElementById('captchaInput').value;
        loginData.captchaResponse = captchaInput;
    }

    // Determine base URL based on environment
    const isLocal = window.location.hostname === 'localhost';
    const baseURL = isLocal ? 'http://localhost:8080' : 'http://192.168.8.100:8080';

    fetch(`${baseURL}/api/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData),
    })
    .then((response) => {
        if (!response.ok) {
            return response.json().then((errorData) => {
                throw errorData;
            });
        }
        return response.json(); // Expecting a JSON response with the token
    })
    .then((data) => {
        // Handle successful login
        localStorage.setItem('authToken', data.token); // Store JWT token in local storage
        window.location.href = '/home/home.html'; // Redirect to the home page
    })
    .catch((error) => {
        let errorMessage = 'Invalid username or password';

        if (error.error === 'ACCOUNT_LOCKED') {
            errorMessage = 'Your account is locked due to too many failed login attempts. Please wait 5 minutes before trying again.';
            document.getElementById('captchaContainer').style.display = 'block'; // Show CAPTCHA after too many failed attempts
            displayCaptcha(); // Generate CAPTCHA when needed
        } else if (error.error === 'PASSWORD_EXPIRED') {
            errorMessage = 'Your password has expired. Please reset your password.';
            window.location.href = '/reset-password.html'; // Redirect to password reset page
            return; // Stop further execution since we're redirecting
        } else if (error.error === 'INTERNAL_SERVER_ERROR') {
            errorMessage = 'An unexpected error occurred. Please try again later.';
        } else if (error.error === 'LOGIN_FAILED') {
            errorMessage = `Invalid username or password. Failed attempts: ${error.failedAttempts}`;
            if (error.failedAttempts >= 3) { // Show CAPTCHA after 3 failed attempts
                document.getElementById('captchaContainer').style.display = 'block';
                displayCaptcha(); // Generate CAPTCHA when needed
            }
        }

        const errorElement = document.getElementById('errorMessage');
        errorElement.textContent = errorMessage;
        errorElement.style.display = 'block'; // Make sure the error message is visible
    });
});

// Add functionality for the reset password button
document.getElementById('resetPasswordBtn').addEventListener('click', function () {
    alert('Redirecting to reset password page'); // Debugging alert
    window.location.href = '/login/reset-password.html'; // Redirect to password reset page
});

// Function to generate a new CAPTCHA
function generateCaptcha() {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let captchaText = '';
    for (let i = 0; i < 6; i++) {
        const randomIndex = Math.floor(Math.random() * characters.length);
        captchaText += characters.charAt(randomIndex);
    }
    return captchaText;
}

// Function to display the generated CAPTCHA
function displayCaptcha() {
    const captcha = generateCaptcha();
    document.getElementById('captchaText').textContent = captcha;
    document.getElementById('captchaInput').value = ''; // Clear previous input
}

// Function to validate the CAPTCHA input
function validateCaptcha() {
    const enteredCaptcha = document.getElementById('captchaInput').value;
    const generatedCaptcha = document.getElementById('captchaText').textContent;
    
    if (enteredCaptcha === generatedCaptcha) {
        return true;
    } else {
        alert('Incorrect CAPTCHA. Please try again.');
        displayCaptcha(); // Generate a new CAPTCHA if the current one is incorrect
        return false;
    }
}

// Initialize CAPTCHA when the page loads
document.addEventListener('DOMContentLoaded', function () {
    // Don't display CAPTCHA on page load
});