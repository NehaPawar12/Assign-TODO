package com.example.assigntodo.auth

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.assigntodo.Users
import com.example.assigntodo.databinding.AccountDialogBinding
import com.example.assigntodo.databinding.ActivitySignUpBinding
import com.example.assigntodo.utils.Utils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private var userImageUri: Uri? = null
    private var userType: String? = null
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        userImageUri = it
        binding.ivUserImage.setImageURI(userImageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            ivUserImage.setOnClickListener {
                selectImage.launch("image/*")
            }
            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                userType = findViewById<RadioButton>(checkedId).text.toString()
            }
            binding.btnRegister.setOnClickListener {
                createUser()
            }
            tvSignIn.setOnClickListener{
                startActivity(Intent(this@SignUpActivity,SignInActivity::class.java))
                finish()
            }
        }


    }

    private fun createUser() {
        Utils.showDialog(this)

        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if(userImageUri == null){
                Utils.showToast(this,"Please select an image")
            }
            else if(password == confirmPassword){
               if (userType!=null)
                    uploadImageUri(name,email,password)
                else{
                   Utils.hideDialog()
                   Utils.showToast(this,"Select User Type")
               }

            }
            else{
                Utils.showToast(this,"Password does not match")
            }
        }
        else{
            Utils.hideDialog()
            Utils.showToast(this,"Please fill all the fields")
        }
    }

    private fun uploadImageUri(name: String, email: String, password: String) {
        val currentUserid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val storageReference = FirebaseStorage.getInstance().getReference("Profile").child(currentUserid).child("Profile.jpg")

        lifecycleScope.launch {
            try{
                val uploadTask = storageReference.putFile(userImageUri!!).await()
                if(uploadTask.task.isSuccessful){
                    val downloadUrl = storageReference.downloadUrl.await()
                    saveUserData(name,email,password,downloadUrl)
                }
                else{
                    Utils.hideDialog()
                    showToast("Upload failed: ${uploadTask.task.exception?.message}")
                }

            }
            catch (e : Exception){
                Utils.hideDialog()
                showToast("Upload failed: ${e.message}")

            }
        }
    }

    private fun saveUserData(name: String, email: String, password: String, downloadUrl: Uri?) {

            lifecycleScope.launch {
                val db = FirebaseDatabase.getInstance().getReference("Users")

                try{
                    val firebaseAuth = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).await()

                    if(firebaseAuth.user != null) {
                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()?.addOnSuccessListener {
                            val dialog = AccountDialogBinding.inflate(LayoutInflater.from(this@SignUpActivity))
                            val alertDialog = AlertDialog.Builder(this@SignUpActivity)
                                .setView(dialog.root)
                                .create()
                            Utils.hideDialog()
                            alertDialog.show()
                            dialog.btnOk.setOnClickListener {
                                alertDialog.dismiss()
                                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                                finish()
                            }
                        }
                        val uId = firebaseAuth.user?.uid.toString()
                        val saveUserType = if(userType == "Boss") "Boss" else "Employee"
                        val boss = Users(userType = saveUserType,userId = uId, userName =  name, userEmail = email, userPassword = password, userImage =  downloadUrl.toString())
                        db.child(uId).setValue(boss).await()

                    }
                    else{
                        Utils.hideDialog()
                        Utils.showToast(this@SignUpActivity, "Signed Up Failed")
                    }
                }
                catch (e : Exception){
                    Utils.hideDialog()
                    Utils.showToast(this@SignUpActivity, e.message.toString())
                }
            }



    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}
