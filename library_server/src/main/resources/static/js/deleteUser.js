document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.delete-user-form').forEach(function(form) {
        form.addEventListener('submit', function(e) {
            if (!confirm('Are you sure you want to delete this user?')) {
                e.preventDefault();
            }
        });
    });
}); 