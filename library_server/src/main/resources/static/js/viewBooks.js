function deleteBooks() {
  const selectedBookIds = [];
  document.querySelectorAll('input[name="book_ids"]:checked').forEach(function (checkbox) {
    selectedBookIds.push(checkbox.value);
  });

  if (selectedBookIds.length === 0) {
    alert('Please select at least one book to delete.');
    return;
  }

  if (confirm('Are you sure you want to delete the selected books?')) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/delete_books';

    const csrfTokenInput = document.querySelector('input[name="_csrf"]');
    if (csrfTokenInput) {
      const csrfInput = document.createElement('input');
      csrfInput.type = 'hidden';
      csrfInput.name = '_csrf';
      csrfInput.value = csrfTokenInput.value;
      form.appendChild(csrfInput);
    }

    selectedBookIds.forEach(function (bookId) {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = 'book_ids';
      input.value = bookId;
      form.appendChild(input);
    });

    document.body.appendChild(form);
    form.submit();
  }
}

function selectAll(source) {
  const checkboxes = document.getElementsByName('book_ids');
  checkboxes.forEach ? checkboxes.forEach(cb => cb.checked = source.checked) :
    Array.from(checkboxes).forEach(cb => cb.checked = source.checked);
}
