package com.yourname.driveexplorer.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE))
            .requestEmail()
            .build()
        
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Check if user is already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        } else {
            // Proceed to Drive Explorer
            showDriveFiles()
        }
    }
    
    private fun showDriveFiles() {
        // TODO: Implement file listing logic
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            // Handle sign-in result
        }
    }
}
