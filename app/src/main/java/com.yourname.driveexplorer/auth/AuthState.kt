sealed class AuthState {
    // Represents different authentication states
    object Loading : AuthState()
    data class Authenticated(val account: GoogleSignInAccount) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val exception: Exception) : AuthState()
}
