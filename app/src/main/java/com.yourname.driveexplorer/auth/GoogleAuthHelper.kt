class GoogleAuthHelper(private val context: Context) {
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE))
                .build()
        )
    }

    fun getSignedInAccount(intent: Intent): GoogleSignInAccount? {
        return GoogleSignIn.getSignedInAccountFromIntent(intent).result
    }
}
