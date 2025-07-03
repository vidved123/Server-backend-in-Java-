document.addEventListener('DOMContentLoaded', function () {
  const usernameInput = document.getElementById('username');
  const suggestionsBox = document.getElementById('suggestions');

  if (!usernameInput || !suggestionsBox) return;

  usernameInput.addEventListener('input', function () {
    const query = this.value;
    if (query.length < 2) {
      suggestionsBox.innerHTML = '';
      return;
    }

    fetch(`/api/usernames?query=${encodeURIComponent(query)}`)
      .then(response => {
        if (!response.ok) throw new Error("Failed to fetch usernames");
        return response.json();
      })
      .then(usernames => {
        suggestionsBox.innerHTML = '';
        usernames.forEach(username => {
          const div = document.createElement('div');
          div.className = 'suggestion-item';
          div.textContent = username;
          div.onclick = () => {
            usernameInput.value = username;
            suggestionsBox.innerHTML = '';
          };
          suggestionsBox.appendChild(div);
        });
      })
      .catch(error => {
        console.error('Error fetching suggestions:', error);
        suggestionsBox.innerHTML = '';
      });
  });
});
