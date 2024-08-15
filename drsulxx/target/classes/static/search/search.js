// Caching function
function cacheResult(key, data) {
    localStorage.setItem(key, JSON.stringify(data));
}

// Retrieving from cache
function getCachedResult(key) {
    const cached = localStorage.getItem(key);
    return cached ? JSON.parse(cached) : null;
}

// Search function
async function searchUser(query) {
    console.log('Search query:', query); // Debugging: check the query

    const lowerQuery = query.toLowerCase();
    let cachedResults = getCachedResult(lowerQuery);
    if (cachedResults) {
        console.log('Returning cached results'); // Debugging: cache hit
        return cachedResults;
    }

    let response;
    const url = (() => {
        if (!isNaN(query)) {
            // Search by ID
            return `/api/search/id/${query}`;
        } else if (query.includes('@')) {
            // Search by Email
            return `/api/search/email/${encodeURIComponent(query)}`;
        } else {
            // Search by Username, Name, or Family Name
            return `/api/search/query/${encodeURIComponent(query)}`;
        }
    })();

    try {
        response = await fetch(url);
        if (response.ok) {
            const data = await response.json();
            console.log('Fetched data:', data); // Debugging: check data from server
            cacheResult(lowerQuery, data);
            return data;
        } else {
            console.error('Error fetching data:', response.statusText);
            return [];
        }
    } catch (error) {
        console.error('Network error:', error);
        return [];
    }
}

// Displaying results
function displayResult(users) {
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = ''; // Clear previous results

    console.log('Displaying results:', users); // Debugging: check the users

    if (Array.isArray(users)) {
        if (users.length > 0) {
            users.forEach(user => {
                const userInfo = `
                    <div class="user-info">
                        <p><strong>ID:</strong> ${user.id}</p>
                        <p><strong>Username:</strong> ${user.username}</p>
                        <p><strong>Name:</strong> ${user.name}</p>
                        <p><strong>Family Name:</strong> ${user.familyName}</p>
                        <p><strong>Email:</strong> ${user.email}</p>
                    </div>
                `;
                resultsDiv.innerHTML += userInfo;
            });
        } else {
            resultsDiv.innerHTML = '<p>No users found.</p>';
        }
    } else if (users && users.id) {
        // Single user result
        const user = users;
        const userInfo = `
            <div class="user-info">
                <p><strong>ID:</strong> ${user.id}</p>
                <p><strong>Username:</strong> ${user.username}</p>
                <p><strong>Name:</strong> ${user.name}</p>
                <p><strong>Family Name:</strong> ${user.familyName}</p>
                <p><strong>Email:</strong> ${user.email}</p>
            </div>
        `;
        resultsDiv.innerHTML = userInfo;
    } else {
        resultsDiv.innerHTML = '<p>User not found.</p>';
    }
}

// Form submission handling
document.getElementById('searchForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const query = document.getElementById('searchInput').value.trim();
    console.log('Form submitted with query:', query); // Debugging: check submitted query
    if (query) {
        const users = await searchUser(query);
        displayResult(users);
    }
});