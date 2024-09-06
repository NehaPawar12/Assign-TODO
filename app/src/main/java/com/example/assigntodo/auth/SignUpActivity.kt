package com.example.assigntodo.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputBinding
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.assigntodo.Boss
import com.example.assigntodo.Employee
import com.example.assigntodo.R
import com.example.assigntodo.databinding.ActivitySignUpBinding
import com.example.assigntodo.utils.Utils
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
                uploadImageUri(name,email,password)
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
        if(userType == "Boss"){
            lifecycleScope.launch {
                val db = FirebaseDatabase.getInstance().getReference("Boss")

                try{
                    val firebaseAuth = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).await()

                    if(firebaseAuth.user != null) {
                        val uId = firebaseAuth.user?.uid.toString()
                        val boss = Boss(uId, name, email, password, downloadUrl.toString())
                        db.child(uId).setValue(boss).await()
                        Utils.hideDialog()
                        Utils.showToast(this@SignUpActivity, "Signed Up Successfully")
                        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
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

        if(userType == "Employee"){
            lifecycleScope.launch {
                val db = FirebaseDatabase.getInstance().getReference("Employee")

                try{
                    val firebaseAuth = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).await()

                    if(firebaseAuth.user != null) {
                        val uId = firebaseAuth.user?.uid.toString()
                        val emp = Employee(uId, name, email, password, downloadUrl.toString())
                        db.child(uId).setValue(emp).await()
                        Utils.hideDialog()
                        Utils.showToast(this@SignUpActivity, "Signed Up Successfully")
                        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
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
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}
