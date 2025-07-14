// Add error handler for all book cover images to comply with CSP

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.book-cover-img').forEach(function(img) {
        img.addEventListener('error', function() {
            this.onerror = null;
            this.src = '/images/default.jpg';
        });
    });
}); 