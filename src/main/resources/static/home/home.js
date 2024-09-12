document.getElementById('logoffButton').addEventListener('click', function () {
    // Determine the current URL for the fetch based on the environment
    const isLocal = window.location.hostname === 'localhost';
    const logoutUrl = isLocal ? 'http://localhost:8080/logout' : 'http://192.168.8.100:8080/logout';

    // Retrieve the token from local storage
    const token = localStorage.getItem('authToken');

    // Perform the logout request
    fetch(logoutUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`  // Include token in the Authorization header
        },
        credentials: 'include'  // Include credentials (cookies) with the request
    })
    .then(response => {
        if (response.ok) {
            // Clear the token from local storage
            localStorage.removeItem('authToken');

            // Redirect to the login page after successful logout
            window.location.href = '/login/login.html';
        } else {
            console.error('Logoff failed');
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
});