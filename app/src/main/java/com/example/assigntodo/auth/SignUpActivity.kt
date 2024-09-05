package com.example.assigntodo.auth

import android.app.Activity
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
import com.example.assigntodo.R
import com.example.assigntodo.databinding.ActivitySignUpBinding
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
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
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                val imageUrl = downloadUrl.toString()
            }
            catch (e : Exception){

            }
        }
    }

}
