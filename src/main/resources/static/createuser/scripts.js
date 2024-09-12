document.addEventListener('DOMContentLoaded', () => {
    const operationSelect = document.getElementById('operation');
    const userForm = document.getElementById('userForm');
    const bulkUserForm = document.getElementById('bulkUserForm');
    const userList = document.getElementById('userList');
    const errorMessage = document.querySelector('.error-message');
    const loadingScreen = document.getElementById('loadingScreen');

    function toggleForms() {
        const isSingleOperation = operationSelect.value === 'single';
        userForm.style.display = isSingleOperation ? 'block' : 'none';
        bulkUserForm.style.display = isSingleOperation ? 'none' : 'block';
    }

    toggleForms();  // Initial form visibility based on default selection
    operationSelect.addEventListener('change', toggleForms);

    // Caching function
    function cacheUser(username, email, user) {
        localStorage.setItem(`${username}-${email}`, JSON.stringify(user));
    }

    // Retrieve from cache
    function getCachedUser(username, email) {
        const cached = localStorage.getItem(`${username}-${email}`);
        return cached ? JSON.parse(cached) : null;
    }

    userForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const username = document.getElementById('username').value.trim();
        const name = document.getElementById('name').value.trim();
        const familyName = document.getElementById('familyName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();

        if (!username || !name || !familyName || !email || !password) {
            showError('All fields are required.');
            return;
        }

        // Check cache before making a request
        let user = getCachedUser(username, email);
        if (user) {
            displayUser(user);
            clearError();
            userForm.reset();
            return;
        }

        try {
            const response = await fetch('/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, name, familyName, email, password })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            if (data.user) {
                cacheUser(username, email, data.user); // Cache the user data
                displayUser(data.user);
                clearError();
                userForm.reset();
                if (!data.emailSent) {
                    showError('User created, but email sending failed.');
                }
            } else {
                showError('Unexpected response from server.');
            }
        } catch (error) {
            showError(`Failed to create user: ${error.message}`);
        }
    });

    bulkUserForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const bulkUsersText = document.getElementById('bulkUsers').value.trim();
        const lines = bulkUsersText.split('\n').filter(line => line.trim() !== '');

        if (lines.length === 0) {
            showError('No user data provided.');
            return;
        }

        // Parse user data
        const users = lines.map(line => {
            const [username, name, familyName, email, password] = line.split(',').map(part => part.trim());
            return { username, name, familyName, email, password };
        });

        // Check for duplicate emails
        const emailSet = new Set();
        const duplicateEmails = users.filter(user => {
            if (emailSet.has(user.email)) {
                return true; // Duplicate email found
            }
            emailSet.add(user.email);
            return false;
        });

        if (duplicateEmails.length > 0) {
            showError('Duplicate email addresses are not allowed: ' + duplicateEmails.map(user => user.email).join(', '));
            return;
        }

        try {
            const response = await fetch('/api/users/bulk', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(users)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            if (Array.isArray(data)) {
                userList.innerHTML = ''; // Clear previous user list
                data.forEach(userResponse => {
                    const user = userResponse.user;
                    cacheUser(user.username, user.email, user); // Cache each user
                    displayUser(user);
                });
                bulkUserForm.reset();
                clearError();
            } else {
                showError('Unexpected response from server.');
            }
        } catch (error) {
            showError(`Failed to create users: ${error.message}`);
        }
    });

    function displayUser(user) {
        const userItem = document.createElement('div');
        userItem.className = 'user-item';
        userItem.textContent = `Username: ${user.username || 'N/A'}, Name: ${user.name || 'N/A'}, Family Name: ${user.familyName || 'N/A'}, Email: ${user.email || 'N/A'}`;
        userList.appendChild(userItem);
    }

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        setTimeout(() => {
            errorMessage.style.display = 'none';
        }, 10000);
    }

    function clearError() {
        errorMessage.textContent = '';
        errorMessage.style.display = 'none';
    }
});