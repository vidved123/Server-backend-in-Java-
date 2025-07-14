// Attach confirmation dialog to Delete Profile button to comply with CSP

document.addEventListener('DOMContentLoaded', function() {
    const deleteBtn = document.getElementById('deleteProfileBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function(event) {
            if (!confirm('Are you sure you want to delete your profile?')) {
                event.preventDefault();
            }
        });
    }
}); 