package com.yourname.driveexplorer.data.model

import android.accounts.Account
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Represents an authenticated user with Drive access
 * @property id Unique identifier from Google
 * @property email User's primary email
 * @property name Full display name
 * @property photoUrl Profile picture URL (nullable)
 * @property grantedScopes Set of authorized API scopes
 * @property account Android Account object for credential storage
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val photoUrl: String?,
    val grantedScopes: Set<String>,
    val account: Account?
) {
    companion object {
        /**
         * Creates a User from GoogleSignInAccount
         * @param googleAccount The signed-in Google account
         */
        fun fromGoogleAccount(googleAccount: GoogleSignInAccount): User {
            return User(
                id = googleAccount.id ?: "",
                email = googleAccount.email ?: "",
                name = googleAccount.displayName ?: "",
                photoUrl = googleAccount.photoUrl?.toString(),
                grantedScopes = googleAccount.grantedScopes?.map { it.scopeUri }?.toSet() ?: emptySet(),
                account = googleAccount.account
            )
        }
    }

    /**
     * Checks if user has Drive access scope
     */
    fun hasDriveAccess(): Boolean {
        return grantedScopes.contains(DriveScopes.DRIVE) ||
               grantedScopes.contains(DriveScopes.DRIVE_FILE)
    }
}
