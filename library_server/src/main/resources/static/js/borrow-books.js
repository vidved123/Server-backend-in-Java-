function loadBooks(search = '') {
  $.get("/borrow/api/books", { search }, function (books) {
    const tbody = $("#book-list").empty();
    books.forEach(book => {
      const disabled = book.availableCopies < 1 ? 'disabled' : '';
      const row = `<tr>
        <td><input type="checkbox" name="bookIds" value="${book.id}" ${disabled}></td>
        <td>${book.id}</td>
        <td><img src="${book.image || '/images/default.jpg'}" width="50" alt="${book.title}"></td>
        <td>${book.title}</td>
        <td>${book.author}</td>
        <td>${book.totalCopies}</td>
        <td>${book.availableCopies}</td>
      </tr>`;
      tbody.append(row);
    });
    $('input[type="checkbox"]').on('change', toggleBorrowButton);
  });
}

function showMessage(message, type = 'info') {
  $('#message-container').html(`<div class="flash-messages ${type}">${message}</div>`);
}

function toggleBorrowButton() {
  const hasBooks = $('input[name="bookIds"]:checked').length > 0;
  const userId = $('#user-id').val()?.trim();
  const validUser = $('#user-id-validation-message').text().toLowerCase().includes("valid");
  $('#borrow-btn').prop('disabled', isAdmin ? !(hasBooks && userId && validUser) : !hasBooks);
}

$(document).ready(function () {
  if (isAdmin) $('#admin-section').show();

  loadBooks();

  $('#search-form').on('submit', function (e) {
    e.preventDefault();
    loadBooks($('#search').val());
  });

  $('#verify-user-id').on('click', function () {
    const userId = $('#user-id').val().trim();
    if (!userId) {
      $('#user-id-validation-message').text("Please enter a User ID.").css("color", "red").show();
      toggleBorrowButton();
      return;
    }

    $.post("/borrow/verify-user-id", { userId }, function (isValid) {
      $('#user-id-validation-message')
        .text(isValid ? "User ID is valid!" : "Invalid User ID!")
        .css("color", isValid ? "green" : "red")
        .show();
      toggleBorrowButton();
    });
  });

  $('#borrow-form').on('submit', function (e) {
    e.preventDefault();
    const bookIds = $('input[name="bookIds"]:checked').map(function () {
      return this.value;
    }).get();
    const data = { bookIds };

    if (isAdmin) {
      const userId = $('#user-id').val()?.trim();
      if (!userId) return;
      data.user_id = userId;
    }

    $.post({
      url: "/borrow",
      data: $.param(data, true),
      success: function () {
        showMessage("Books borrowed successfully!", "info");
        loadBooks();
      },
      error: function () {
        showMessage("Error borrowing books.", "error");
      }
    });
  });

  $('#delete-books').on('click', function () {
    const selected = $('input[name="bookIds"]:checked');
    if (selected.length === 0) {
      alert("Please select at least one book to delete.");
      return;
    }
    const bookIds = selected.map(function () { return this.value; }).get();
    console.log("Delete book IDs:", bookIds);
    // TODO: Implement delete logic here
  });
});
