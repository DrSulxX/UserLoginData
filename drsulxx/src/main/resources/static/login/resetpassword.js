document.addEventListener('DOMContentLoaded', function () {
    // Initially hide reset form and token verification form
    document.getElementById('resetFormContainer').style.display = 'none';
    document.getElementById('verifyTokenContainer').style.display = 'none';

    // Handle email form submission
    document.getElementById('emailForm').addEventListener('submit', function (event) {
        event.preventDefault();

        const email = document.getElementById('email').value;

        fetch('/api/password/forgot', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: email }),
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || 'Error sending reset link');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log("Data received", data);
            // Show token verification form after successful email sending
            document.getElementById('emailFormContainer').style.display = 'none';
            document.getElementById('verifyTokenContainer').style.display = 'block';
            document.getElementById('verifyMessage').textContent = data.message || 'Enter the token sent to your email.';
            document.getElementById('verifyMessage').style.display = 'block';
        })
        .catch(error => {
            console.error("Error encountered", error);
            document.getElementById('emailErrorMessage').textContent = error.message || 'Error sending reset link. Please try again.';
            document.getElementById('emailErrorMessage').style.display = 'block';
        });
    });

    // Handle token verification form submission
    document.getElementById('verifyTokenForm').addEventListener('submit', function (event) {
        event.preventDefault();

        const token = document.getElementById('verifyToken').value;

        fetch('/api/password/verify-token', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token: token }),
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || 'Invalid or expired reset token');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log("Data received", data);
            // Show reset password form if token is valid
            document.getElementById('verifyTokenContainer').style.display = 'none';
            document.getElementById('resetFormContainer').style.display = 'block';
            document.getElementById('resetMessage').textContent = data.message || 'Enter your new password.';
            document.getElementById('resetMessage').style.display = 'block';
        })
        .catch(error => {
            console.error("Error encountered", error);
            document.getElementById('verifyTokenErrorMessage').textContent = error.message || 'Error verifying token. Please try again.';
            document.getElementById('verifyTokenErrorMessage').style.display = 'block';
        });
    });

    // Handle reset form submission
    document.getElementById('resetForm').addEventListener('submit', function (event) {
        event.preventDefault();

        const token = document.getElementById('verifyToken').value;  // Use the token from the verification step
        const newPassword = document.getElementById('newPassword').value;

        fetch('/api/password/reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token: token, newPassword: newPassword }),
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || 'Failed to reset password');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log("Data received", data);
            alert(data.message || 'Password has been reset successfully.');
            window.location.href = '/login/login.html'; // Redirect to login page
        })
        .catch(error => {
            console.error("Error encountered", error);
            document.getElementById('resetErrorMessage').textContent = error.message || 'Error resetting password. Please try again.';
            document.getElementById('resetErrorMessage').style.display = 'block';
        });
    });
});