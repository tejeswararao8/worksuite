import toast from 'react-hot-toast';

/**
 * Extracts a human-readable message from an Axios error response.
 * Handles Spring Boot ApiResponse envelope, validation errors, and network errors.
 */
export function getErrorMessage(error) {
  if (!error) return 'An unexpected error occurred';

  // Network / no response
  if (!error.response) return 'Network error. Please check your connection.';

  const { status, data } = error.response;

  // Spring Boot ApiResponse envelope: { success, message, data }
  if (data?.message) return data.message;

  // Spring Boot validation errors: { errors: { field: msg } }
  if (data?.errors) {
    const first = Object.values(data.errors)[0];
    if (first) return first;
  }

  switch (status) {
    case 400: return 'Invalid request. Please check your input.';
    case 401: return 'Session expired. Please log in again.';
    case 403: return 'You do not have permission to perform this action.';
    case 404: return 'The requested resource was not found.';
    case 409: return 'A record with this information already exists.';
    case 423: return 'Account is locked. Please try again later.';
    case 500: return 'Server error. Please try again later.';
    default:  return `Error ${status}. Please try again.`;
  }
}

/** Shows a toast error with the extracted message. */
export function toastError(error) {
  toast.error(getErrorMessage(error));
}

/** Shows a toast success. */
export function toastSuccess(message) {
  toast.success(message);
}
