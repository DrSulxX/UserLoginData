document.getElementById('logoffButton').addEventListener('click', function () {
    // Determine the current URL for the fetch
    const isMobile = window.location.hostname === '192.168.8.100';
    const logoutUrl = isMobile ? 'http://192.168.8.100:8080/logout' : 'http://localhost:8080/logout';

    // Retrieve the token from local storage
    const token = localStorage.getItem('authToken');

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

            // Redirect to login page after successful logout
            window.location.href = '/login/login.html';
        } else {
            console.error('Logoff failed');
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
});