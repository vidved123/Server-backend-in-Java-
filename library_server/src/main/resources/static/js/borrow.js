document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing verification...');
    console.log('Current page URL:', window.location.href);
    
    const adminSection = document.getElementById('admin-section');
    const verifyBtn = document.getElementById('verifyUserBtn');
    const userIdInput = document.getElementById('user_id');
    const verificationStatus = document.getElementById('verificationStatus');
    const borrowForm = document.getElementById('borrowForm');
    const borrowBtn = document.getElementById('borrowBtn');
    
    console.log('Admin section found:', !!adminSection);
    console.log('Verify button found:', !!verifyBtn);
    console.log('User ID input found:', !!userIdInput);
    console.log('Verification status element found:', !!verificationStatus);
    console.log('Borrow form found:', !!borrowForm);
    console.log('Borrow button found:', !!borrowBtn);
    
    // Log all elements with admin-related IDs
    console.log('All elements with admin-section ID:', document.querySelectorAll('#admin-section'));
    console.log('All elements with verifyUserBtn ID:', document.querySelectorAll('#verifyUserBtn'));
    
    let isUserVerified = false;
    let verifiedUserId = null;
    const BORROW_LIMIT = 3;
    
    if (adminSection && verifyBtn) {
        console.log('Setting up verification button...');
        
        verifyBtn.addEventListener('click', function() {
            console.log('Verify button clicked');
            const userId = userIdInput.value.trim();
            console.log('User ID entered:', userId);
            
            if (!userId) {
                showVerificationStatus('Please enter a User ID', 'error');
                return;
            }
            
            // Disable verify button and show loading
            verifyBtn.disabled = true;
            verifyBtn.textContent = 'Verifying...';
            showVerificationStatus('Verifying user...', 'info');
            
            console.log('Making AJAX call to verify user:', userId);
            
            // Make AJAX call to verify user
            fetch('/borrow/verify-user-id', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'userId=' + encodeURIComponent(userId)
            })
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Verification result:', data);
                if (data) {
                    showVerificationStatus('✅ User verified successfully! Books will be borrowed for User ID: ' + userId, 'success');
                    isUserVerified = true;
                    verifiedUserId = userId;
                    borrowBtn.disabled = false;

                    // Update the user_id input field for both forms
                    if (userIdInput) {
                        userIdInput.value = userId;
                    }
                } else {
                    showVerificationStatus('❌ User not found. Please check the User ID.', 'error');
                    isUserVerified = false;
                    verifiedUserId = null;
                    borrowBtn.disabled = true;
                    if (userIdInput) userIdInput.value = '';
                }
            })
            .catch(error => {
                console.error('Verification error:', error);
                showVerificationStatus('❌ Error verifying user. Please try again.', 'error');
                isUserVerified = false;
                verifiedUserId = null;
                borrowBtn.disabled = true;
            })
            .finally(() => {
                verifyBtn.disabled = false;
                verifyBtn.textContent = 'Verify User';
            });
        });
        
        // Reset verification when user ID changes
        userIdInput.addEventListener('input', function() {
            if (isUserVerified) {
                isUserVerified = false;
                verifiedUserId = null;
                showVerificationStatus('', '');
                borrowBtn.disabled = true;
            }
        });
        
        // Prevent form submission if user not verified (for admins)
        borrowForm.addEventListener('submit', function(e) {
            if (adminSection && !isUserVerified) {
                e.preventDefault();
                showVerificationStatus('⚠️ Please verify the User ID before borrowing books.', 'error');
                return false;
            }
            
            // Ensure the verified user ID is included in the form submission
            if (isUserVerified && verifiedUserId) {
                console.log('Submitting form for verified user ID:', verifiedUserId);
                // The form will automatically include the user_id field with the verified ID
            }
        });
        
        // Initially disable borrow button for admins
        if (adminSection) {
            borrowBtn.disabled = true;
        }
        
        // Set up book selection tracking
        setupBookSelectionTracking();
    } else {
        console.log('Admin section or verify button not found');
        // Set up book selection tracking even for non-admin users
        setupBookSelectionTracking();
    }
    
    // Book selection persistence across searches
    const searchForm = document.getElementById('searchForm');
    const selectedBookIdsInput = document.getElementById('selectedBookIds');

    // Before submitting the search form, store selected book IDs
    if (searchForm && selectedBookIdsInput) {
        searchForm.addEventListener('submit', function() {
            const checkedBoxes = document.querySelectorAll('input[name="bookIds"]:checked');
            const selectedIds = Array.from(checkedBoxes).map(cb => cb.value);
            selectedBookIdsInput.value = selectedIds.join(',');
        });
    }

    // On page load, re-check checkboxes for selected IDs from hidden input (if present)
    if (selectedBookIdsInput && selectedBookIdsInput.value) {
        const selectedIds = selectedBookIdsInput.value.split(',');
        selectedIds.forEach(id => {
            const checkbox = document.getElementById('book_' + id);
            if (checkbox) checkbox.checked = true;
        });
    }

    function showVerificationStatus(message, type) {
        console.log('Showing status:', message, type);
        if (verificationStatus) {
            verificationStatus.textContent = message;
            verificationStatus.className = 'verification-status ' + type;
        }
    }
    
    function setupBookSelectionTracking() {
        const checkboxes = document.querySelectorAll('input[name="bookIds"]');
        const selectedCountSpan = document.getElementById('selectedCount');
        const borrowBtn = document.getElementById('borrowBtn');
        
        function updateSelectionCount() {
            const selectedCount = document.querySelectorAll('input[name="bookIds"]:checked').length;
            if (selectedCountSpan) {
                selectedCountSpan.textContent = selectedCount;
                
                // Update color based on selection count
                if (selectedCount === 0) {
                    selectedCountSpan.style.color = '#feb2b2'; // Red for 0
                } else if (selectedCount <= BORROW_LIMIT) {
                    selectedCountSpan.style.color = '#9ae6b4'; // Green for valid
                } else {
                    selectedCountSpan.style.color = '#fbbf24'; // Yellow for over limit
                }
            }
            
            // Enable/disable borrow button based on selection
            if (borrowBtn) {
                const adminSection = document.getElementById('admin-section');
                const isAdmin = !!adminSection;
                
                if (isAdmin) {
                    // For admins, button is disabled until user is verified AND books are selected
                    borrowBtn.disabled = !isUserVerified || selectedCount === 0;
                } else {
                    // For regular users, button is disabled if no books are selected
                    borrowBtn.disabled = selectedCount === 0;
                }
            }
            
            // Show warning if over limit
            if (selectedCount > BORROW_LIMIT) {
                showBorrowLimitWarning();
            } else {
                hideBorrowLimitWarning();
            }
        }
        
        function showBorrowLimitWarning() {
            let warning = document.getElementById('borrowLimitWarning');
            if (!warning) {
                warning = document.createElement('div');
                warning.id = 'borrowLimitWarning';
                warning.className = 'borrow-limit-warning';
                warning.innerHTML = '⚠️ You can only borrow up to 3 books at a time. Please deselect some books.';
                
                const borrowInfo = document.querySelector('.borrow-info');
                if (borrowInfo) {
                    borrowInfo.appendChild(warning);
                }
            }
        }
        
        function hideBorrowLimitWarning() {
            const warning = document.getElementById('borrowLimitWarning');
            if (warning) {
                warning.remove();
            }
        }
        
        // Add event listeners to all checkboxes
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const selectedCount = document.querySelectorAll('input[name="bookIds"]:checked').length;
                
                // Prevent selecting more than the limit
                if (this.checked && selectedCount > BORROW_LIMIT) {
                    this.checked = false;
                    alert('You can only borrow up to 3 books at a time.');
                    return;
                }
                
                updateSelectionCount();
            });
        });
        
        // Initial count update
        updateSelectionCount();
    }
});