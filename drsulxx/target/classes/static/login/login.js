document.getElementById('loginForm').addEventListener('submit', function (event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const loginData = {
        username: username,
        password: password
    };

    // Determine base URL based on environment
    const isLocal = window.location.hostname === 'localhost';
    const baseURL = isLocal ? 'http://localhost:8080' : 'http://192.168.8.100:8080';

    fetch(`${baseURL}/api/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(loginData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(errorData => {
                throw errorData;
            });
        }
        return response.json();  // Expecting a JSON response with the token
    })
    .then(data => {
        // Handle successful login
        console.log(data);  // Log the response or handle it as needed

        // Save the token to local storage (or session storage, depending on your preference)
        localStorage.setItem('authToken', data.token);  // Store JWT token in local storage

        window.location.href = '/home/home.html';  // Redirect to the home page
    })
    .catch(error => {
        // Enhanced error handling based on the backend response
        let errorMessage = 'Invalid username or password';

        if (error.error === 'ACCOUNT_LOCKED') {
            errorMessage = 'Your account is locked due to too many failed login attempts. Please wait 5 minutes before trying again.';
        } else if (error.error === 'PASSWORD_EXPIRED') {
            errorMessage = 'Your password has expired. Please reset your password.';
            window.location.href = '/reset-password.html';  // Redirect to password reset page
            return;  // Stop further execution since we're redirecting
        } else if (error.error === 'INTERNAL_SERVER_ERROR') {
            errorMessage = 'An unexpected error occurred. Please try again later.';
        } else if (error.error === 'LOGIN_FAILED') {
            errorMessage = `Invalid username or password. Failed attempts: ${error.failedAttempts}`;
        }

        // Display the error message
        const errorElement = document.getElementById('errorMessage');
        errorElement.textContent = errorMessage;
        errorElement.style.display = 'block'; // Make sure the error message is visible

        console.error('Error:', error);
    });
});