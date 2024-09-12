document.getElementById('logoffButton').addEventListener('click', function () {
    fetch('http://localhost:8080/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/login/login.html';  // Absolute path to login.html
        } else {
            console.error('Logoff failed');
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
});